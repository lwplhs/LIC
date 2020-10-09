package com.lwp.website.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: liweipeng
 * @Date: 2020/05/15/10:58
 * @Description:
 */
@Component
@ConfigurationProperties(prefix = "website")
@PropertySource(value = "config.properties")
@Order(value=1)
public class SysConfig {

    private String defaultCookie;

    private String priKey;

    private String loginSessionKey;

    private String userInCookie;

    private String aesSalt;

    private String loginErrorCount;

    private String loginUser;

    private String licensePath;

    private String userSession;

    private int shiroRedisSaveTime;

    public int getShiroRedisSaveTime() {
        return shiroRedisSaveTime;
    }

    public void setShiroRedisSaveTime(int shiroRedisSaveTime) {
        this.shiroRedisSaveTime = shiroRedisSaveTime;
    }

    public String getDefaultCookie() {
        return defaultCookie;
    }

    public void setDefaultCookie(String defaultCookie) {
        this.defaultCookie = defaultCookie;
    }


    public String getLoginErrorCount() {
        return loginErrorCount;
    }

    public void setLoginErrorCount(String loginErrorCount) {
        this.loginErrorCount = loginErrorCount;
    }

    public String getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }

    public String getLoginSessionKey() {
        return loginSessionKey;
    }

    public void setLoginSessionKey(String loginSessionKey) {
        this.loginSessionKey = loginSessionKey;
    }

    public String getUserInCookie() {
        return userInCookie;
    }

    public void setUserInCookie(String userInCookie) {
        this.userInCookie = userInCookie;
    }

    public String getAesSalt() {
        return aesSalt;
    }

    public void setAesSalt(String aesSalt) {
        this.aesSalt = aesSalt;
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }

    public String getLicensePath() {
        return licensePath;
    }

    public void setLicensePath(String licensePath) {
        this.licensePath = licensePath;
    }

    public String getUserSession() {
        return userSession;
    }

    public void setUserSession(String userSession) {
        this.userSession = userSession;
    }
}
