package com.miaomiao.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.miaomiao.entity.Weibo;
import com.miaomiao.utils.HttpClientUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.apache.http.HttpHeaders.REFERER;

/**
 * Created by lyl on 2017-8-8.
 */
@Component
public class WeiboService {
    private static final Logger logger = LoggerFactory.getLogger(WeiboService.class);

    //相关接口url
    public static final String LOGIN_API_URL = "https://passport.weibo.cn/sso/login";
    public static final String LOTTERY_URL = "https://m.weibo.cn/api/container/getIndex?type=all&queryVal=%E6%8A%BD%E5%A5%96&featurecode=20000320&luicode=10000011&lfid=106003type%3D1&title=%E6%8A%BD%E5%A5%96&containerid=100103type%3D1%26q%3D%E6%8A%BD%E5%A5%96&page=2";
    public static final String FOLLOW_URL = "http://s.weibo.com/ajax/usercard/relation?fajtype=follow&__rnd=1485012958054";
    //请求头信息
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";
    public static final String PRAGMA = "no-cache";
    public static final String LOGIN_REFERER = "https://passport.weibo.cn/signin/login?entry=mweibo&r=http%3A%2F%2Fweibo.cn%2F&backTitle=%CE%A2%B2%A9&vt=";
    public static final String WEIBO_REFERER = "https://weibo.cn/u/123";

    //post body
    public static final String FOLLOW_BODY="uid=%s&f=1&extra=refer_flag: 0000020001_|refer_lflag: 1001030103_|loca: |refer_sort: &refer_sort=card&refer_flag=0000020001_&location=&oid=&wforce=1&nogroup=false&_t=0";

    public static final String LOGIN_QUERY = "savestate=1&r=http%3A%2F%2Fweibo.cn%2F&ec=0&pagerefer=&entry=mweibo&wentry=&loginfrom=&client_id=&code=&qq=&mainpageflag=1&hff=&hfp=";

    public static String COOKIES = null;

    public static String login(String username, String password) throws Exception {
        HttpClient httpClient = HttpClientUtils.getHttpClient();
        HttpPost post = HttpClientUtils.getHttpPost(LOGIN_API_URL, LOGIN_REFERER);

        String query = String.format("username=%s&password=%s&", username, password) + LOGIN_QUERY;
        StringEntity reqEntity = new StringEntity(query);
        post.setEntity(reqEntity);
        HttpResponse httpResponse = httpClient.execute(post);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new Exception("登录失败，请检查用户名与密码");
        }
        Header[] headers = httpResponse.getAllHeaders();
        String[] cookies = Arrays.stream(headers).filter(header -> header.getName().equalsIgnoreCase("set-cookie"))
                .map(header -> header.getValue().replace("(.*=\\S*;)\\s*expires.*", "$1"))
                .toArray(String[]::new);

        COOKIES = String.join(" ", cookies);
        return COOKIES;
    }

    public static void handleWeibo(Weibo weibo, String username, String password) {
        String cookies = getLoginCookies(username, password);
        boolean successFollowed = follow(weibo, cookies);
        if (cookies == null) {
            return;
        }

        if (weibo.isNeedForwarded()) {
            return;
        }
        if (weibo.isNeedReplayed()) {

        }
    }

    private static synchronized String getLoginCookies(String username, String password) {
        String loginCookies = WeiboService.COOKIES;
        if (loginCookies == null) {
            try {
                loginCookies = WeiboService.login(username, password);
            } catch (Exception e) {
                logger.error(e.getMessage());
                return null;
            }
        }
        return loginCookies;
    }

    private static boolean follow(Weibo weibo, String cookies) {
        HttpClient httpClient = HttpClientUtils.getHttpClient();
        HttpPost httpPost = HttpClientUtils.getHttpPost(FOLLOW_URL,WEIBO_REFERER,cookies);
        try {
            String query = String.format(FOLLOW_BODY, weibo.getUid());
            StringEntity reqEntity = new StringEntity(query);
            httpPost.setEntity(reqEntity);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                return false;
            }
            String jsonString = EntityUtils.toString(httpResponse.getEntity());
            JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();
            if (object.get("code").getAsString().equals("100000")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
