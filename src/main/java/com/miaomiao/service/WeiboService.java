package com.miaomiao.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.miaomiao.dto.WeiboRepository;
import com.miaomiao.entity.Weibo;
import com.miaomiao.utils.HttpClientUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;

@Component
public class WeiboService {
    private static final Logger logger = LoggerFactory.getLogger(WeiboService.class);

    //相关接口url
    public static final String LOTTERY_URL = "https://m.weibo.cn/api/container/getIndex?type=all&queryVal=%E6%8A%BD%E5%A5%96&featurecode=20000320&luicode=10000011&lfid=106003type%3D1&title=%E6%8A%BD%E5%A5%96&containerid=100103type%3D1%26q%3D%E6%8A%BD%E5%A5%96&page=2";
    public static final String LOGIN_API_URL = "https://passport.weibo.cn/sso/login";
    public static final String FOLLOW_URL = "http://s.weibo.com/ajax/user/follow?__rnd=1504271463393";
    public static final String FORWARD_URL = "http://s.weibo.com/ajax/mblog/forward?__rnd=1485084296329";
    public static final String REPLY_URL = "http://s.weibo.com/ajax/comment/add?__rnd=1484832107695";
    //请求头信息
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";
    public static final String PRAGMA = "no-cache";
    public static final String LOGIN_REFERER = "https://passport.weibo.cn/signin/login?entry=mweibo&r=http%3A%2F%2Fweibo.cn%2F&backTitle=%CE%A2%B2%A9&vt=";
    public static final String WEIBO_REFERER = "http://s.weibo.com/weibo/%25E6%258A%25BD%25E5%25A5%2596?topnav=1&wvr=6&b=1";

    //post body
    public static final String FOLLOW_BODY = "uid=%s&type=followed&wforce=&_t=0";
    public static final String FORWARD_BODY = "appkey=&mid=%s&style_type=1&reason=%s&location=&_t=0";
    public static final String REPLY_BODY = "act=post&mid=%s&uid=%s&forward=0&isroot=0&content=%s&pageid=weibo&_t=0";
    public static final String LOGIN_QUERY = "savestate=1&r=http%3A%2F%2Fweibo.cn%2F&ec=0&pagerefer=&entry=mweibo&wentry=&loginfrom=&client_id=&code=&qq=&mainpageflag=1&hff=&hfp=";

    public static String COOKIES = null;

    private static WeiboRepository weiboRepository;

    @Autowired()
    public void setWeiboRepository(WeiboRepository weiboRepository) {
        WeiboService.weiboRepository = weiboRepository;
    }

    private static String login(String username, String password) {
        logger.info("{} start login, time: {}", username, new Date());
        HttpPost post = HttpClientUtils.getHttpPost(LOGIN_API_URL, LOGIN_REFERER);
        String query = String.format("username=%s&password=%s&", username, password) + LOGIN_QUERY;

        HttpResponse httpResponse = null;
        String result;
        try {
            StringEntity reqEntity = new StringEntity(query);
            post.setEntity(reqEntity);
            httpResponse = HttpClientUtils.getPostResponse(post, query);
            result = HttpClientUtils.getPostResponseBody(httpResponse);
        } catch (Exception e) {
            result = null;
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        if (result == null) {
            logger.warn("登录失败，请检查用户名与密码");
            return null;
        }

        Header[] headers = httpResponse.getAllHeaders();
        String[] cookies = Arrays.stream(headers).filter(header -> header.getName().equalsIgnoreCase("set-cookie"))
                .map(header -> header.getValue().replaceFirst("([^=]*=[^;]*;).*", "$1"))
                .toArray(String[]::new);

        COOKIES = String.join(" ", cookies);
        logger.info("{} login success, cookies:{}, time: {}", username, cookies, new Date());
        return COOKIES;
    }

    public static void handleWeibo(Weibo weibo, String username, String password) {
        logger.info("Mid:{} start job,time:{}", weibo.getMid(), new Date());
        String cookies = getLoginCookies(username, password);
        if (cookies == null) {
            return;
        }

        boolean successFollowed = follow(weibo, cookies);
        weibo.setFollowed(successFollowed);
        logger.info("Mid:{} End Follow Job, State: {}, time:{}", weibo.getMid(), successFollowed ? "success" : "failed", new Date());

        if (weibo.isNeedReply()) {
            boolean successReplyed = reply(weibo, cookies);
            weibo.setReplyed(successReplyed);
            logger.info("Mid:{} End Reply Job, State{}, Time:{}", weibo.getMid(), successReplyed ? "success" : "failed", new Date());
        }

        if (weibo.isNeedForward()) {
            boolean successForwarded = forward(weibo, cookies);
            weibo.setForwarded(successForwarded);
            logger.info("Mid:{} End Forward Job, State: {}, time:{}", weibo.getMid(), successForwarded ? "success" : "failed", new Date());
        }

        if (weibo.isNeedForward() == weibo.isForwarded()
                && weibo.isNeedReply() == weibo.isReplyed()) {
            weibo.setCompleted(true);
        }
        weiboRepository.save(weibo);
        logger.info("Mid:{} End Job, Time:{}", weibo.getMid(), new Date());
    }

    private static synchronized String getLoginCookies(String username, String password) {
        String loginCookies = WeiboService.COOKIES;
        if (loginCookies == null) {
            try {
                loginCookies = WeiboService.login(username, password);
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn(e.getMessage());
                loginCookies = null;
            }
        }
        return loginCookies;
    }

    private static boolean follow(Weibo weibo, String cookies) {
        HttpPost httpPost = HttpClientUtils.getHttpPost(FOLLOW_URL, WEIBO_REFERER, cookies);
        try {
            String query = String.format(FOLLOW_BODY, weibo.getUid());
            String result = HttpClientUtils.getPostResponseBody(httpPost, query);
            JsonObject object = new JsonParser().parse(result).getAsJsonObject();
            if (object.get("code").getAsString().equals("100000")) {
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    private static boolean forward(Weibo weibo, String cookies) {
        HttpPost httpPost = HttpClientUtils.getHttpPost(FORWARD_URL, WEIBO_REFERER, cookies);
        try {
            //设置转发内容 默认为二哈表情加话题
            String reason = "[二哈][二哈] " + weibo.getTopic();
            String topic = URLEncoder.encode(reason, "utf-8");
            String query = String.format(FORWARD_BODY, weibo.getMid(), topic);
            String result = HttpClientUtils.getPostResponseBody(httpPost, query);
            JsonObject object = new JsonParser().parse(result).getAsJsonObject();
            if (object.get("code").getAsString().equals("100000")) {
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    private static boolean reply(Weibo weibo, String cookies) {
        HttpPost httpPost = HttpClientUtils.getHttpPost(REPLY_URL, WEIBO_REFERER, cookies);
        try {
            //设置评论内容 默认为二哈表情加话题
            String comment = "[二哈][二哈] " + weibo.getTopic();
            String topic = URLEncoder.encode(comment, "utf-8");
            String query = String.format(REPLY_BODY, weibo.getMid(), weibo.getUid(), topic);
            String result = HttpClientUtils.getPostResponseBody(httpPost, query);
            JsonObject object = new JsonParser().parse(result).getAsJsonObject();
            if (object.get("code").getAsString().equals("100000")) {
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }
}
