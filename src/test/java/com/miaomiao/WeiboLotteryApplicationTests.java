package com.miaomiao;

import com.miaomiao.service.WeiboService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WeiboLotteryApplicationTests {
	@Value("${weibo.username}")
	private String username;

	@Value("${weibo.password}")
	private String password;


}
