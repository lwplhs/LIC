package com.lwp.website.utils;

import com.lwp.website.config.SysConfig;
import com.lwp.website.entity.Vo.UserVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: liweipeng
 * @Date: 2020/06/27/12:16
 * @Description:
 */
@Component
public class TaleUtils {


    private static Logger LOGGER = LoggerFactory.getLogger(TaleUtils.class);

    private static SysConfig sysConfig;

    private static RedisUtil redisUtil;

    @Autowired
    public void setSysConfig(SysConfig sysConfig){
        TaleUtils.sysConfig = sysConfig;
    }

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil){
        TaleUtils.redisUtil = redisUtil;
    }

    /**
     * 返回当前登录用户
     *
     * @return
     */
    public static UserVo getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        UserVo userVo = null;
        if (null == session) {
            userVo = TaleUtils.getLoginUserByRedis(request);
        }else {
            userVo = (UserVo) session.getAttribute(sysConfig.getLoginSessionKey());
            if(StringUtil.isNull(userVo)){
                userVo = TaleUtils.getLoginUserByRedis(request);
            }
        }
        return userVo;
    }

    public static UserVo getLoginUserByRedis(HttpServletRequest request){
        String defaultCookie = sysConfig.getDefaultCookie();
        String loginUserKey = sysConfig.getLoginUser();
        UserVo userVo = null;
        Cookie cookie = TaleUtils.cookieRaw(defaultCookie, request);
        if(cookie != null ) {
            String cookieValue = cookie.getValue();
            Object obj = redisUtil.get(loginUserKey + cookieValue);
            userVo = (UserVo) obj;
        }
        return userVo;
    }

    /**
     * 从cookies中获取指定cookie
     *
     * @param name    名称
     * @param request 请求
     * @return cookie
     */
    public static Cookie cookieRaw(String name, HttpServletRequest request) {
        javax.servlet.http.Cookie[] servletCookies = request.getCookies();
        if (servletCookies == null) {
            return null;
        }
        for (javax.servlet.http.Cookie c : servletCookies) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    /**
     * 获取cookie中的用户id
     *
     * @param request
     * @return
     */
    public static String getCookieUid(HttpServletRequest request) {
        if (null != request) {
            Cookie cookie = cookieRaw(sysConfig.getUserInCookie(), request);
            if (cookie != null && cookie.getValue() != null) {
                try {
                    String uid = AesUtil.deAes(cookie.getValue(), sysConfig.getAesSalt());
                    return StringUtils.isNotBlank(uid) ? uid : null;
                } catch (Exception e) {
                }
            }
        }
        return null;
    }
    /**
     * md5加密
     *
     * @param source 数据源
     * @return 加密字符串
     */
    public static String MD5encode(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ignored) {
        }
        byte[] encode = messageDigest.digest(source.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte anEncode : encode) {
            String hex = Integer.toHexString(0xff & anEncode);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    /**
     * 设置记住密码cookie
     *
     * @param response
     * @param uid
     */
    public static void setCookie(HttpServletResponse response, String uid) {
        try {
            String val = AesUtil.enAes(uid, sysConfig.getAesSalt());
            boolean isSSL = false;
            Cookie cookie = new Cookie(sysConfig.getUserInCookie(), val);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 30);
            cookie.setSecure(isSSL);
            response.addCookie(cookie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能:压缩多个文件成一个zip文件
     * @param srcfile：源文件列表
     * @param zipfile：压缩后的文件
     */
    public static void zipFiles(File[] srcfile, File zipfile) {
        byte[] buf = new byte[1024];
        try {
            //ZipOutputStream类：完成文件或文件夹的压缩
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
            for (int i = 0; i < srcfile.length; i++) {
                FileInputStream in = new FileInputStream(srcfile[i]);
                // 给列表中的文件单独命名
                out.putNextEntry(new ZipEntry(srcfile[i].getName()));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
            LOGGER.info("压缩完成........");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取 license保存位置
     * @return
     */
    public static String getLicensePath(){
        String content = "";
        try {
            content = sysConfig.getLicensePath();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(StringUtil.isNull(content)){
            content = "/media/license";
        }
        String path = System.getProperty("user.dir");
        String url = content;
        path = path.replaceAll("\\\\","/");


        url = addSeparator(0,"/",url);
        url = addSeparator(1,"/",url);

        String date = StringUtil.getDate(new Date(),"yyyyMMdd");
        url = url + date;

        String temp = String.valueOf(System.currentTimeMillis());
        url = addSeparator(1,"/",url);
        url = url + temp;
        path = path + url;
        return path;
    }

    /**
     * 给字符串头和尾增加分隔符
     * @param index 0 头 1 尾
     * @param separator 分隔符
     * @param arg 字符串
     * @return
     */
    private static String addSeparator(int index,String separator,String arg){
        switch (index){
            case 0:
                if(!arg.startsWith(separator)){
                    arg = separator+arg;
                }
                break;
            case 1:
                if(!arg.endsWith(separator)){
                    arg = arg + separator;
                }
                break;
            default:
                break;
        }
        return arg;
    }

}
