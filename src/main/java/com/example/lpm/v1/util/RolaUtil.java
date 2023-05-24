package com.example.lpm.v1.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RolaUtil {

    private RolaUtil() {}

    public static String randomUsername() {
        return "skyescn_" + randomUserNum();
    }

    public static int randomUserNum() {
        return RandomUtil.randomInt(100000, 110000);
    }

    public static String refresh(String user, String country, String state, String city) {
        // http://refresh.rola.info/refresh?user=skyescn_1&country=us&state=&city=
        StringBuilder rolaUrl = new StringBuilder("http://refresh.rola.info/refresh?user=" + user + "&country=");
        rolaUrl.append(CharSequenceUtil.replace(country, " ", "").toLowerCase());
        rolaUrl.append("&state=");
        if (CharSequenceUtil.isNotBlank(state)) {
            rolaUrl.append(CharSequenceUtil.replace(state, " ", "").toLowerCase());
        }
        rolaUrl.append("&city=");
        if (CharSequenceUtil.isNotBlank(city)) {
            rolaUrl.append(CharSequenceUtil.replace(city, " ", "").toLowerCase());
        }
        log.info("rola refresh url :{}", rolaUrl);
        String result = HttpUtil.get(String.valueOf(rolaUrl));
        log.info("rola refresh result :{} ", result);
        return result;
    }

    public static String refresh(String url, String user, String country, String state, String city) {
        // http://refresh.rola.info/refresh?user=skyescn_1&country=us&state=&city=
        StringBuilder rolaUrl = new StringBuilder(url + "?user=" + user + "&country=");
        rolaUrl.append(CharSequenceUtil.replace(country, " ", "").toLowerCase());
        rolaUrl.append("&state=");
        if (CharSequenceUtil.isNotBlank(state)) {
            rolaUrl.append(CharSequenceUtil.replace(state, " ", "").toLowerCase());
        }
        rolaUrl.append("&city=");
        if (CharSequenceUtil.isNotBlank(city)) {
            rolaUrl.append(CharSequenceUtil.replace(city, " ", "").toLowerCase());
        }
        log.info("rola refresh url :{}", rolaUrl);
        String result = HttpUtil.get(String.valueOf(rolaUrl));
        log.info("rola refresh result :{} ", result);
        return result;
    }

}
