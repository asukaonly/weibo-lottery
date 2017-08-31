package com.miaomiao.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.miaomiao.service.WeiboService;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by lyl on 2017-8-10.
 */
public class HttpClientUtils {
    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
    private static HttpClient HTTP_CLIENT;

    public synchronized static HttpClient getHttpClient() {
        if (HTTP_CLIENT == null) {
            HTTP_CLIENT = HttpClients.createDefault();
        }
        return HTTP_CLIENT;
    }

    public static HttpResponse get(String url) throws Exception {
        HttpClient httpClient = getHttpClient();
        HttpGet httpGet = new HttpGet(url);
        return httpClient.execute(httpGet);
    }

    public static JsonObject getResponseJson(String url) throws Exception {
        HttpResponse httpResponse = get(url);
        String responseBody = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        JsonObject responseJson;
        responseJson = new JsonParser().parse(responseBody).getAsJsonObject();
        return responseJson;
    }

    public static HttpPost getHttpPost(String url) {
        HttpPost post = new HttpPost(url);
        post.setHeader(HttpHeaders.CONTENT_TYPE, WeiboService.CONTENT_TYPE);
        post.setHeader(HttpHeaders.USER_AGENT, WeiboService.USER_AGENT);
        post.setHeader(HttpHeaders.PRAGMA, WeiboService.PRAGMA);
        return post;
    }

    public static HttpPost getHttpPost(String url,String referer) {
        HttpPost post = getHttpPost(url);
        post.setHeader(HttpHeaders.REFERER, referer);
        return post;
    }

    public static HttpPost getHttpPost(String url,String referer,String cookies) {
        HttpPost post = getHttpPost(url,referer);
        post.setHeader("Cookie", cookies);
        return post;
    }

    public static HttpGet getHttpGet(String url) {
        HttpGet get = new HttpGet(url);
        get.setHeader(HttpHeaders.CONTENT_TYPE, WeiboService.CONTENT_TYPE);
        get.setHeader(HttpHeaders.USER_AGENT, WeiboService.USER_AGENT);
        get.setHeader(HttpHeaders.PRAGMA, WeiboService.PRAGMA);
        return get;
    }

    public static HttpGet getHttpGet(String url,String referer) {
        HttpGet get = getHttpGet(url);
        get.setHeader(HttpHeaders.REFERER, referer);
        return get;
    }

    public static HttpGet getHttpGet(String url,String referer,String cookies) {
        HttpGet get = getHttpGet(url,referer);
        get.setHeader("Cookie", cookies);
        return get;
    }
}
