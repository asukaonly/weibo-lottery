package com.miaomiao.job;

import com.miaomiao.entity.Weibo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by lyl on 2017-8-8.
 */
@Component
public class ScheduledWeiboMonitorJob {
    @Value("${weibo.username}")
    private String username;

    @Value("${weibo.password}")
    private String password;

    //30分钟执行1次
    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void weiboMonitorJob() {


        System.out.println("现在时间：" + new Date());
    }
}
