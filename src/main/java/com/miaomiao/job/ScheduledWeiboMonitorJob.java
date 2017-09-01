package com.miaomiao.job;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.miaomiao.dto.WeiboRepository;
import com.miaomiao.entity.Weibo;
import com.miaomiao.utils.HttpClientUtils;
import com.miaomiao.service.WeiboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lyl on 2017-8-8.
 */
@Component
public class ScheduledWeiboMonitorJob {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${weibo.username}")
    private String username;

    @Value("${weibo.password}")
    private String password;

    @Autowired
    private WeiboRepository weiboRepository;

    //30分钟执行1次
    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void weiboMonitorJob() {
        //获取带抽奖关键字的微博
        logger.info("weibo monitor job start");
        JsonObject responseJson = null;
        try {
            responseJson = HttpClientUtils.getResponseJson(WeiboService.LOTTERY_URL);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }

        if (responseJson.get("ok").getAsInt() != 1) {
            logger.error("接口请求失败，请重试");
            //todo 失败向邮箱发送日志
            return;
        }


        JsonArray cards = responseJson.get("cards").getAsJsonArray();
        //过滤热门微博
        JsonArray contents= new JsonArray();
        for (JsonElement element:cards){
            JsonObject o=element.getAsJsonObject();
            if (!o.has("title") || !o.get("title").getAsString().equals("热门微博")) {
                contents.addAll(o.get("card_group").getAsJsonArray());
            }
        }
        List<Weibo> weiboList=new ArrayList<Weibo>();
        for (JsonElement content : contents) {
            JsonObject contentJson = content.getAsJsonObject().get("mblog").getAsJsonObject();
            String mid=contentJson.get("mid").getAsString();

            Weibo weibo = weiboRepository.findOne(mid);
            if (weibo!=null)
                continue;

            weibo=new Weibo();
            weibo.setUid(contentJson.get("user").getAsJsonObject().get("id").getAsString());
            weibo.setMid(mid);
            String text = contentJson.get("text").getAsString();
            weibo.setContent(text);
            weibo.setNeedForward(text.contains("转发"));
            weibo.setNeedReply(text.contains("评论"));
            weibo.setFollowed(false);
            weibo.setForwarded(false);
            weibo.setReplyed(false);

            StringBuilder topicString = new StringBuilder();
            List<String> matches = new ArrayList<>();
            Pattern p = Pattern.compile("#[^#]*#");
            Matcher matcher = p.matcher(text);
            while (matcher.find()){
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

            if (weibo.isNeedForward()||weibo.isNeedReply()){
                weibo.setCompleted(false);
                weiboList.add(weibo);
            }else {
                weibo.setCompleted(true);
                weiboRepository.save(weibo);
            }

            //weiboRepository.save(weibo);
        }
        weiboList.stream().parallel().forEach(weibo -> WeiboService.handleWeibo(weibo,username,password));
    }

}
