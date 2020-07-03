package com.lwp.website.utils;

import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: liweipeng
 * @Date: 2020/06/27/14:55
 * @Description:
 */
@Component
public class Commons {
    public static String THEME = "themes/default";
    /**
     * 获取随机数
     *
     * @param max
     * @param str
     * @return
     */
    public static String random(int max, String str) {
        return UUID.random(1, max) + str;
    }
}
