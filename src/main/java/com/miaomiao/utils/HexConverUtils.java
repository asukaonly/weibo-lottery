package com.miaomiao.utils;

/**
 * @author miaomiao
 */
public class HexConverUtils {
    /**
     * bytes转换成十六进制字符串
     *
     * @param b byte数组
     * @return String 小写16进制字符串
     */
    public static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
        }
        return sb.toString().toLowerCase().trim();
    }
}
