package com.miaomiao.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.plaf.PanelUI;
import java.io.IOException;

/**
 * Created by lyl on 2017-8-8.
 */
public class WeiboService {

    //登录页面url
    public static final String LOGIN_API_URL="https://passport.weibo.cn/sso/login";
    public static final String CONTENT_TYPE="Content-Type: application/x-www-form-urlencoded";
    public static final String USER_AGENT="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";
    public static final String HOST="Host: passport.weibo.cn";
    public static final String ORIGIN="Origin: https://passport.weibo.cn";
    public static final String PRAGMA="Pragma: no-cache";
    public static final String REFERER="Referer: https://passport.weibo.cn/signin/login?entry=mweibo&r=http%3A%2F%2Fweibo.cn%2F&backTitle=%CE%A2%B2%A9&vt=";
    public static final String REQUEST_HEAD=CONTENT_TYPE

    public static String login(){

        try {

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return "";
    }

}
