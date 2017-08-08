package com.miaomiao;

import com.miaomiao.entity.Weibo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackageClasses = Weibo.class)
public class WeiboLotteryApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeiboLotteryApplication.class, args);
	}
}
