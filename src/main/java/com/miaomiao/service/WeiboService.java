package com.miaomiao.service;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by lyl on 2017-8-8.
 */
@Component
public class WeiboService {
    //相关接口url
    public static final String LOGIN_API_URL="https://passport.weibo.cn/sso/login";
    public static final String LOTTERY_URL="https://m.weibo.cn/api/container/getIndex?type=all&queryVal=%E6%8A%BD%E5%A5%96&featurecode=20000320&luicode=10000011&lfid=106003type%3D1&title=%E6%8A%BD%E5%A5%96&containerid=100103type%3D1%26q%3D%E6%8A%BD%E5%A5%96&page=";

    //请求头信息
    public static final String CONTENT_TYPE="application/x-www-form-urlencoded";
    public static final String USER_AGENT="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";
    public static final String HOST="passport.weibo.cn";
    public static final String ORIGIN="https://passport.weibo.cn";
    public static final String PRAGMA="no-cache";
    public static final String REFERER="https://passport.weibo.cn/signin/login?entry=mweibo&r=http%3A%2F%2Fweibo.cn%2F&backTitle=%CE%A2%B2%A9&vt=";

    public static final String QUERY="savestate=1&r=http%3A%2F%2Fweibo.cn%2F&ec=0&pagerefer=&entry=mweibo&wentry=&loginfrom=&client_id=&code=&qq=&mainpageflag=1&hff=&hfp=";

    public static String COOKIES=null;

    public static String login(String username,String password) throws Exception{
        HttpClient httpClient= HttpClients.createDefault();
        HttpPost post=getHttpPost();
        String query=String.format("username=%s&password=%s&",username,password)+QUERY;
        StringEntity reqEntity = new StringEntity(query);
        post.setEntity(reqEntity);
        HttpResponse httpResponse=httpClient.execute(post);
        if (httpResponse.getStatusLine().getStatusCode()!=200){
            throw new Exception("登录失败，请检查用户名与密码");
        }
        Header[] headers=httpResponse.getAllHeaders();
        String[] cookies=Arrays.stream(headers).filter(header -> header.getName().toLowerCase().equals("set-cookie"))
                            .map(header -> header.getValue().replace("(.*=\\S*;)\\s*expires.*","$1"))
                            .toArray(String[]::new);
        COOKIES=String.join(" ",cookies);
        return COOKIES;
    }

    private static String getRequestHead(){
        return String.join("\n",CONTENT_TYPE,USER_AGENT,HOST,ORIGIN,PRAGMA,REFERER);
    }

    private static HttpPost getHttpPost() throws UnsupportedEncodingException {
        HttpPost post=new HttpPost(LOGIN_API_URL);
        post.setHeader("Content-Type",CONTENT_TYPE);
        post.setHeader("User-Agent",USER_AGENT);
        post.setHeader("Host",HOST);
        post.setHeader("Origin",ORIGIN);
        post.setHeader("Pragma",PRAGMA);
        post.setHeader("Referer",REFERER);
        return post;
    }
}
