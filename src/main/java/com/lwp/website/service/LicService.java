package com.lwp.website.service;

import com.alibaba.fastjson.JSONObject;
import com.lwp.website.controller.LicController;
import com.lwp.website.entity.Vo.LicVo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: liweipeng
 * @Date: 2020/06/28/15:56
 * @Description:
 */
public interface LicService {

    List<LicVo> listLic(Map map);

    Map insertLic(LicVo licVo);

    Boolean deleteLicByIds(String ids);

    void downloadLicense(HttpServletResponse response,String id) throws IOException;

    String getTotal(Map map);

}
