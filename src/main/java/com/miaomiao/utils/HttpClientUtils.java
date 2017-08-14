package com.miaomiao.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by lyl on 2017-8-10.
 */
public class HttpClientUtils {
    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
    private static HttpClient HTTP_CLIENT;

    private static HttpClient getHttpClient() {
        if (HTTP_CLIENT == null) {
            HTTP_CLIENT = HttpClients.createDefault();
        }
        return HTTP_CLIENT;
    }

    public static HttpResponse get(String url) throws IOException {
        HttpClient httpClient = getHttpClient();
        HttpGet httpGet = new HttpGet(url);
        return httpClient.execute(httpGet);
    }

    public static JsonObject getResponseJson(String url) throws Exception {
        HttpResponse httpResponse = get(url);
        String responseBody = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        JsonObject responseJson = null;
        responseJson = new JsonParser().parse(responseBody).getAsJsonObject();
        return responseJson;
    }
}
