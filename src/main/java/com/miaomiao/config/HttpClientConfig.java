package com.miaomiao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author miaomiao
 */
@Component
@ConfigurationProperties(prefix = "httpclient")
public class HttpClientConfig {
    public static int MAX_TOTAL_CONNECTIONS;

    public static int DEFAULT_MAX_PER_ROUTE;

    public static int MAX_RETRY_COUNT;

    public final static String CHROME_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36";
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    @Value("${httpclient.maxTotalConnections}")
    public static void setMaxTotalConnections(int maxTotalConnections) {
        HttpClientConfig.MAX_TOTAL_CONNECTIONS = maxTotalConnections;
    }

    @Value("${httpclient.defaultMaxPerRoute}")
    public static void setDefaultMaxPerRoute(int defaultMaxPerRoute) {
        HttpClientConfig.DEFAULT_MAX_PER_ROUTE = defaultMaxPerRoute;
    }

    @Value("${httpclient.maxRetryCount}")
    public static void setMaxRetryCount(int maxRetryCount) {
        HttpClientConfig.MAX_RETRY_COUNT = maxRetryCount;
    }
}
