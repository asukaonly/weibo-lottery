package com.miaomiao.utils;


import com.miaomiao.config.HttpClientConfig;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

public class HttpClientUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtils.class);

    private static PoolingHttpClientConnectionManager cm = null;
    private static CloseableHttpClient HTTP_CLIENT = null;

    private static void initClientPool() {
        LOGGER.info("Init Client Pool...");
        LayeredConnectionSocketFactory sslSocketFactory = null;
        try {
            sslSocketFactory = new SSLConnectionSocketFactory(SSLContext.getDefault());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslSocketFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        cm.setMaxTotal(HttpClientConfig.MAX_TOTAL_CONNECTIONS);
        cm.setDefaultMaxPerRoute(HttpClientConfig.DEFAULT_MAX_PER_ROUTE);
        LOGGER.info("Init Client Pool Success");
    }

    private static HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            // 如果已经重试了5次，就放弃
            if (executionCount >= HttpClientConfig.MAX_RETRY_COUNT) {
                return false;
            }
            // 如果服务器丢掉了连接，那么就重试
            if (exception instanceof NoHttpResponseException) {
                return true;
            }
            // 不要重试SSL握手异常
            if (exception instanceof SSLHandshakeException) {
                return false;
            }
            // 超时
            if (exception instanceof InterruptedIOException) {
                return false;
            }
            // 目标服务器不可达
            if (exception instanceof UnknownHostException) {
                return false;
            }
            // ssl握手异常
            if (exception instanceof SSLException) {
                return false;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求是幂等的，就再次尝试
            if (!(request instanceof HttpEntityEnclosingRequest)) {
                return true;
            }
            return false;
        }
    };

    private synchronized static CloseableHttpClient getHttpClient() {
        if (cm == null) {
            initClientPool();
        }
        if (HTTP_CLIENT == null) {
            HTTP_CLIENT = HttpClients.custom()
                    .setConnectionManager(cm)
                    .setRetryHandler(httpRequestRetryHandler)
                    .build();
        }
        return HTTP_CLIENT;
    }

    public static void closeHttpClient() throws IOException {
        if (HTTP_CLIENT != null) {
            HTTP_CLIENT.close();
            HTTP_CLIENT = null;
        }
    }

    //region getHttpPost
    private static HttpPost getHttpPost(String url) {
        return new HttpPost(url);
    }

    public static HttpPost getHttpPost(String url, String ua) {
        HttpPost post = getHttpPost(url);
        if (ua != null) {
            post.setHeader(HttpHeaders.USER_AGENT, ua);
        } else {
            post.setHeader(HttpHeaders.USER_AGENT, HttpClientConfig.CHROME_UA);
        }
        return post;
    }

    public static HttpPost getHttpPost(String url, String ua, String referer) {
        HttpPost post = getHttpPost(url, ua);
        if (referer != null) {
            post.setHeader(HttpHeaders.REFERER, referer);
        }
        return post;
    }

    public static HttpPost getHttpPost(String url, String ua, String referer, String cookies) {
        HttpPost post = getHttpPost(url, ua, referer);
        if (cookies != null) {
            post.setHeader("Cookie", cookies);
        }
        return post;
    }

    public static HttpPost getHttpPost(String url, Header... httpHeaders) {
        HttpPost post = getHttpPost(url);
        for (Header header : httpHeaders) {
            post.setHeader(header);
        }
        return post;
    }
    //endregion

    //region getHttpGet
    private static HttpGet getHttpGet(String url) {
        return new HttpGet(url);
    }

    public static HttpGet getHttpGet(String url, String ua) {
        HttpGet get = getHttpGet(url);
        if (ua != null) {
            get.setHeader(HttpHeaders.USER_AGENT, ua);
        } else {
            get.setHeader(HttpHeaders.USER_AGENT, HttpClientConfig.CHROME_UA);
        }
        return get;
    }

    public static HttpGet getHttpGet(String url, String ua, String referer) {
        HttpGet get = getHttpGet(url, ua);
        if (referer != null) {
            get.setHeader(HttpHeaders.REFERER, referer);
        }
        return get;
    }

    public static HttpGet getHttpGet(String url, String ua, String referer, String cookies) {
        HttpGet get = getHttpGet(url, ua, referer);
        if (cookies != null) {
            get.setHeader("Cookie", cookies);
        }
        return get;
    }

    public static HttpGet getHttpGet(String url, Header... httpHeaders) {
        HttpGet get = getHttpGet(url);
        for (Header header : httpHeaders) {
            get.setHeader(header);
        }
        return get;
    }
    //endregion

    public static CloseableHttpResponse getResponse(HttpGet httpGet) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        return httpClient.execute(httpGet, HttpClientContext.create());
    }

    public static CloseableHttpResponse postResponse(HttpPost httpPost) throws Exception {
        return getHttpClient().execute(httpPost);
    }

    public static CloseableHttpResponse postResponse(HttpPost httpPost, String query) throws Exception {
        StringEntity reqEntity = new StringEntity(query);
        httpPost.setEntity(reqEntity);
        return postResponse(httpPost);
    }

    public static HttpEntity getResponseEntity(String url) throws Exception {
        HttpGet httpGet = getHttpGet(url);
        return getResponseEntity(getResponse(httpGet));
    }

    public static HttpEntity getResponseEntity(CloseableHttpResponse httpResponse) throws Exception {
        if (HttpStatus.SC_OK == httpResponse.getStatusLine().getStatusCode()) {
            return httpResponse.getEntity();
        }
        return null;
    }

    public static String getResponseBody(CloseableHttpResponse httpResponse) throws Exception {
        HttpEntity entity = getResponseEntity(httpResponse);
        if (entity != null) {
            String responseBody = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);
            httpResponse.close();
            return responseBody;
        }
        return null;
    }

    public static String get(String url) {
        return get(url, null);
    }

    public static String get(String url, String referer) {
        return get(url, referer, null);
    }

    public static String get(String url, String referer, String ua) {
        return get(url, referer, ua, null);
    }

    public static String get(String url, String referer, String ua, String cookies) {
        HttpGet httpGet = getHttpGet(url, referer, ua, cookies);
        try {
            CloseableHttpResponse response = getResponse(httpGet);
            return getResponseBody(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String post(String url) {
        return post(url, null);
    }

    public static String post(String url, String referer) {
        return post(url, referer, null);
    }

    public static String post(String url, String referer, String ua) {
        return post(url, referer, ua, null);
    }

    public static String post(String url, String referer, String ua, String cookies) {
        HttpPost httpPost = getHttpPost(url, referer, ua, cookies);
        try {
            CloseableHttpResponse response = postResponse(httpPost);
            return getResponseBody(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
