package com.miaomiao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HttpClientConfig {
    @Value("${weibo.username}")
    public String MAX_TOTAL_CONNECTIONS;

    @Value("${weibo.username}")
    public String DEFAULT_MAX_PER_ROUTE;

    @Value("${weibo.username}")
    public String MAX_RETRY_COUNT;

    @Value("${weibo.username}")
    public String CHROME_UA;
}
