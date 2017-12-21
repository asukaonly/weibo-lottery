package com.miaomiao.job;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.miaomiao.dto.WeiboRepository;
import com.miaomiao.entity.Weibo;
import com.miaomiao.service.WeiboService;
import com.miaomiao.utils.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

@Component
public class ScheduledWeiboMonitorJob {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${weibo.username}")
    private String username;

    @Value("${weibo.password}")
    private String password;

    @Autowired
    private WeiboRepository weiboRepository;


    //2分钟执行1次
    @Scheduled(fixedDelay = 2 * 60 * 1000)
    public void weiboMonitorJob() {
        //获取带抽奖关键字的微博
        logger.info("Weibo Monitor Job Start, Time:{}", new Date());
        String rsp = HttpClientUtils.get(WeiboService.LOTTERY_URL);
        if (rsp == null) {
            logger.info("接口请求失败，无法获取信息, 时间:{}", new Date());
            return;
        }
        JsonObject responseJson;
        try {
            responseJson = new JsonParser().parse(rsp).getAsJsonObject();
        } catch (IllegalStateException e) {
            logger.error("json转换失败, json string: {}, date: {}", rsp, new Date());
            return;
        }

        if (responseJson.get("ok").getAsInt() != 1) {
            logger.error("接口请求失败，请重试");
            return;
        }

        JsonArray cards = responseJson.get("data").getAsJsonObject().get("cards").getAsJsonArray();
        //过滤热门微博
        JsonArray contents = new JsonArray();
        for (JsonElement element : cards) {
            JsonObject o = element.getAsJsonObject();
            if (!o.has("title") || !o.get("title").getAsString().equals("热门微博")) {
                if (o.has("card_group")) {
                    contents.addAll(o.get("card_group").getAsJsonArray());
                }
            }
        }
        List<Weibo> weiboList = new ArrayList<>();
        for (JsonElement content : contents) {
            JsonObject contentJson = content.getAsJsonObject().get("mblog").getAsJsonObject();
            String mid = contentJson.get("mid").getAsString();

            Weibo weibo = weiboRepository.findOne(mid);
            if (weibo != null)
                continue;

            weibo = new Weibo();
            weibo.setUid(contentJson.get("user").getAsJsonObject().get("id").getAsString());
            weibo.setMid(mid);
            String text = contentJson.get("text").getAsString();
            //过滤emoji
            text = text.replaceAll(WeiboService.EMOJI_REGEX, "");
            weibo.setContent(text);
            weibo.setNeedForward(text.contains("转发"));
            weibo.setNeedReply(text.contains("评论"));
            weibo.setFollowed(false);
            weibo.setForwarded(false);
            weibo.setReplyed(false);

            StringBuilder topicString = new StringBuilder();
            List<String> matches = new ArrayList<>();
            Matcher matcher = WeiboService.TOPIC_REGEX.matcher(text);
            while (matcher.find()) {
                matches.add(matcher.group());
            }
            for (String match : matches) {
                if (match.length() > 20) {
                    //过滤超长话题
                    continue;
                }
                topicString.append(" ").append(match);
                if (topicString.length() > 200) {
                    //话题过多则跳出
                    topicString = new StringBuilder(topicString.substring(0, 200));
                    break;
                }
            }

            weibo.setTopic(topicString.toString());
            weibo.setUpdateDate(new Date());

            if (weibo.isNeedForward() || weibo.isNeedReply()) {
                weibo.setCompleted(false);
                weiboList.add(weibo);
            } else {
                weibo.setCompleted(true);
                weiboRepository.save(weibo);
            }
        }
        //weiboList.stream().parallel().forEach(weibo ->WeiboService.handleWeibo(weibo, username, password));

        //单线程运行，并发调用会转发评论失败
        for (Weibo weibo : weiboList) {
            WeiboService.handleWeibo(weibo, username, password);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            HttpClientUtils.closeHttpClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Weibo Monitor Job End, Time:{}", new Date());
    }

}
