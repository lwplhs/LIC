package com.lwp.website.entity.Vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: liweipeng
 * @Date: 2020/06/28/14:38
 * @Description:
 */
public class LicVo {
    private String id;
    private String unitName;
    private String licCode;
    private String isMac;
    private String mac;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date ex;
    private String concurrent;
    private String license;

    private String isDelete;

    private Date ORA;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getLicCode() {
        return licCode;
    }

    public void setLicCode(String licCode) {
        this.licCode = licCode;
    }

    public String getIsMac() {
        return isMac;
    }

    public void setIsMac(String isMac) {
        this.isMac = isMac;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Date getEx() {
        return ex;
    }

    public void setEx(Date ex) {
        this.ex = ex;
    }

    public String getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(String concurrent) {
        this.concurrent = concurrent;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(String isDelete) {
        this.isDelete = isDelete;
    }

    public Date getORA() {
        return ORA;
    }

    public void setORA(Date ORA) {
        this.ORA = ORA;
    }
}
