package com.miaomiao.job;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.miaomiao.entity.Weibo;
import com.miaomiao.utils.HttpClientUtils;
import com.miaomiao.service.WeiboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by lyl on 2017-8-8.
 */
@Component
public class ScheduledWeiboMonitorJob {
    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @Value("${weibo.username}")
    private String username;

    @Value("${weibo.password}")
    private String password;

    //30分钟执行1次
    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void weiboMonitorJob() {
        String loginCookies=WeiboService.COOKIES;
        if (loginCookies==null){
            try {
                loginCookies=WeiboService.login(username,password);
            }catch (Exception e){
                logger.error(e.getMessage());
                return;
            }
        }

        JsonObject responseJson=null;
        try {
            responseJson=HttpClientUtils.getResponseJson(WeiboService.LOTTERY_URL);
        } catch (Exception e) {
            return;
        }

        if (responseJson.get("ok").getAsInt()!=1){
            logger.error("接口请求失败，请重试");
            return;
        }

        JsonArray contents=responseJson.get("cards").getAsJsonArray()
                            .get(0).getAsJsonObject()
                            .get("card_group").getAsJsonArray();
        for (JsonElement content:contents){
            JsonObject contentJson=content.getAsJsonObject().get("mblog").getAsJsonObject();


        }

    }
}
