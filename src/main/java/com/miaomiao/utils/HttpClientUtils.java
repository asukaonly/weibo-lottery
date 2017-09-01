package com.miaomiao.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.miaomiao.service.WeiboService;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * Created by lyl on 2017-8-10.
 */
public class HttpClientUtils {
    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
    private static CloseableHttpClient HTTP_CLIENT;

    public synchronized static CloseableHttpClient getHttpClient() {
        if (HTTP_CLIENT == null) {
            HTTP_CLIENT = HttpClients.createDefault();
        }
        return HTTP_CLIENT;
    }

    public static void closeHttpClient() throws IOException {
        if (HTTP_CLIENT != null) {
            HTTP_CLIENT.close();
        }
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

    public static HttpPost getHttpPost(String url, String referer) {
        HttpPost post = getHttpPost(url);
        post.setHeader(HttpHeaders.REFERER, referer);
        return post;
    }

    public static HttpPost getHttpPost(String url, String referer, String cookies) {
        HttpPost post = getHttpPost(url, referer);
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

    public static HttpGet getHttpGet(String url, String referer) {
        HttpGet get = getHttpGet(url);
        get.setHeader(HttpHeaders.REFERER, referer);
        return get;
    }

    public static HttpGet getHttpGet(String url, String referer, String cookies) {
        HttpGet get = getHttpGet(url, referer);
        get.setHeader("Cookie", cookies);
        return get;
    }

    public static HttpResponse getPostResponse(HttpPost httpPost) throws Exception {
        HttpResponse httpResponse = getHttpClient().execute(httpPost);
        return httpResponse;
    }

    public static HttpResponse getPostResponse(HttpPost httpPost, String query) throws Exception {
        StringEntity reqEntity = new StringEntity(query);
        httpPost.setEntity(reqEntity);
        return getPostResponse(httpPost);
    }

    public static String getPostResponseBody(HttpResponse httpResponse) throws Exception {
        return EntityUtils.toString(httpResponse.getEntity());
    }

    public static String getPostResponseBody(HttpPost httpPost, String query) throws Exception {
        HttpResponse httpResponse = getPostResponse(httpPost, query);
        if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200) {
            return getPostResponseBody(httpResponse);
        }
        return null;
    }
}
