package com.miaomiao.job;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.miaomiao.dto.JPAEntity;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
        JsonObject responseJson = null;
        try {
            responseJson = HttpClientUtils.getResponseJson(WeiboService.LOTTERY_URL);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }

        if (responseJson.get("ok").getAsInt() != 1) {
            logger.error("接口请求失败，请重试");
            return;
        }

        JsonArray contents = responseJson.get("cards").getAsJsonArray()
                .get(0).getAsJsonObject()
                .get("card_group").getAsJsonArray();
        List<Weibo> weiboList=new ArrayList<Weibo>();
        for (JsonElement content : contents) {
            JsonObject contentJson = content.getAsJsonObject().get("mblog").getAsJsonObject();
            Weibo weibo = new Weibo();
            weibo.setId(UUID.randomUUID().toString());
            weibo.setUid(contentJson.get("user").getAsJsonObject().get("id").getAsString());
            weibo.setMid(contentJson.get("mid").getAsString());
            String text = contentJson.get("text").getAsString();
            weibo.setContent(text);
            weibo.setNeedForwarded(text.contains("转发"));
            weibo.setNeedReplayed(text.contains("评论"));
            weibo.setFollowed(false);
            weibo.setForwarded(false);
            weibo.setReplayed(false);
            weibo.setCompleted(false);
            Pattern p = Pattern.compile("#[^#]*#");
            StringBuilder topicString = new StringBuilder();
            String[] matches = p.split(text);
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
            weiboList.add(weibo);
        }
        weiboList.stream().parallel().forEach(weibo -> WeiboService.handleWeibo(weibo,username,password));
    }

}
