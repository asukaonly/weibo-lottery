package com.miaomiao.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.miaomiao.dto.WeiboRepository;
import com.miaomiao.entity.Weibo;
import com.miaomiao.utils.HttpClientUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;

@Component
public class WeiboService {
    public static final long NOW = System.currentTimeMillis();
    //相关接口url
    public static final String LOTTERY_URL = "https://m.weibo.cn/api/container/getIndex?type=all&queryVal=%E6%8A%BD%E5%A5%96&featurecode=20000320&luicode=10000011&lfid=106003type%3D1&title=%E6%8A%BD%E5%A5%96&containerid=100103type%3D1%26q%3D%E6%8A%BD%E5%A5%96&page=2";
    public static final String PRE_LOGIN_API_URL =  "https://login.sina.com.cn/sso/prelogin.php?entry=weibo&callback=sinaSSOController.preloginCallBack&su=%%su&rsakt=mod&checkpin=1&client=ssologin.js(v1.4.19)&_=%%time";
    public static final String LOGIN_API_URL =  "https://passport.weibo.cn/sso/login";
    public static final String FOLLOW_URL = "http://s.weibo.com/ajax/user/follow?__rnd=" + NOW;
    public static final String FORWARD_URL = "http://s.weibo.com/ajax/mblog/forward?__rnd=" + NOW;
    public static final String REPLY_URL = "http://s.weibo.com/ajax/comment/add?__rnd=" + NOW;
    //请求头信息
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";
    public static final String PRAGMA = "no-cache";
    public static final String LOGIN_REFERER = "https://passport.weibo.cn/signin/login?entry=mweibo&r=http%3A%2F%2Fweibo.cn%2F&backTitle=%CE%A2%B2%A9&vt=";
    public static final String WEIBO_REFERER = "http://s.weibo.com/weibo/%25E6%258A%25BD%25E5%25A5%2596?topnav=1&wvr=6&b=1";
    public static final String WEIBO_LOGIN_REFERER="https://weibo.com/";
    //post body
    public static final String FOLLOW_BODY = "uid=%%uid&type=followed&f=1&extra=refer_flag%3A0000020001_%7Crefer_lflag%3A1001030103_%7Cloca%3A%7Crefer_sort%3A&refer_sort=card&refer_flag=0000020001_&location=&oid=&wforce=1&nogroup=false&_t=0";
    public static final String FORWARD_BODY = "appkey=&mid=%s&style_type=1&reason=%s&location=&_t=0";
    public static final String REPLY_BODY = "act=post&mid=%s&uid=%s&forward=0&isroot=0&content=%s&pageid=weibo&_t=0";
    public static final String LOGIN_QUERY = "savestate=1&r=http%3A%2F%2Fweibo.cn%2F&ec=0&pagerefer=&entry=mweibo&wentry=&loginfrom=&client_id=&code=&qq=&mainpageflag=1&hff=&hfp=";

    public static final String emojiRegex="(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|[\uD83C\uDD70\uD83C\uDD71\uD83C\uDD7E\uD83C\uDD7F\uD83C\uDD8E\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|[\u0023\u002A\u0030-\u0039]\uFE0F?\u20E3|[\u2194-\u2199\u21A9-\u21AA]\uFE0F?|[\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55]\uFE0F?|[\u2934\u2935]\uFE0F?|[\u3030\u303D]\uFE0F?|[\u3297\u3299]\uFE0F?|[\uD83C\uDE01\uD83C\uDE02\uD83C\uDE1A\uD83C\uDE2F\uD83C\uDE32-\uD83C\uDE3A\uD83C\uDE50\uD83C\uDE51]\uFE0F?|[\u203C\u2049]\uFE0F?|[\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE]\uFE0F?|[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|[\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA]\uFE0F?)";

    private static final Logger logger = LoggerFactory.getLogger(WeiboService.class);
    public static String COOKIES = null;

    private static WeiboRepository weiboRepository;

    private static String login(String username, String password) {
        logger.info("{} Start Login, Time: {}", username, new Date());

        try {
            String base64Username=new BASE64Encoder().encode(URLEncoder.encode(username,"utf-8").getBytes());
            String preLoginUrl=PRE_LOGIN_API_URL.replace("%%su",base64Username)
                    .replace("%%time",String.valueOf(System.currentTimeMillis()));
            HttpGet preLoginHttpGet=HttpClientUtils.getHttpGet(preLoginUrl,WEIBO_LOGIN_REFERER);
            String preLoginUrlRsp=
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

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
        logger.info("{} Login Success, Cookies: {}, Time: {}", username, cookies, new Date());
        return COOKIES;
    }

    public static void handleWeibo(Weibo weibo, String username, String password) {
        logger.info("Mid:{} Start Job, Time: {}", weibo.getMid(), new Date());
        String cookies = getLoginCookies(username, password);
        if (cookies == null) {
            return;
        }

        boolean successFollowed = follow(weibo, cookies);
        weibo.setFollowed(successFollowed);
        logger.info("Mid:{} End Follow Job, Status: {}, time:{}", weibo.getMid(), successFollowed ? "success" : "failed", new Date());

        if (weibo.isNeedReply()) {
            boolean successReplyed = reply(weibo, cookies);
            weibo.setReplyed(successReplyed);
            logger.info("Mid:{} End Reply Job, Status: {}, Time:{}", weibo.getMid(), successReplyed ? "success" : "failed", new Date());
        }

        if (weibo.isNeedForward()) {
            boolean successForwarded = forward(weibo, cookies);
            weibo.setForwarded(successForwarded);
            logger.info("Mid:{} End Forward Job, Status: {}, time:{}", weibo.getMid(), successForwarded ? "success" : "failed", new Date());
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
            String query = FOLLOW_BODY.replace("%%uid",weibo.getUid());
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

    @Autowired()
    public void setWeiboRepository(WeiboRepository weiboRepository) {
        WeiboService.weiboRepository = weiboRepository;
    }
}
