package com.lwp.website.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lwp.website.entity.Bo.RestResponseBo;
import com.lwp.website.entity.Vo.LicVo;
import com.lwp.website.service.LicService;
import com.lwp.website.utils.StringUtil;
import com.lwp.website.utils.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: liweipeng
 * @Date: 2020/06/27/18:05
 * @Description:
 */
@Controller
@RequestMapping(value = "/lic")
public class LicController extends BaseController{


    private Logger LOGGER = LoggerFactory.getLogger(LicController.class);

    @Resource
    private LicService licService;

    @RequestMapping(value = "/lic-add")
    public String toLicAdd(Model model){
        model.addAttribute("licCode", UUID.UU32());
        return this.render("/lic-add");
    }

    @RequestMapping(value = "/lic-list")
    public String toLicList(){

        return this.render("/lic-list");

    }

    @RequestMapping(value = "/getTotal")
    @ResponseBody
    public String getTotal(@RequestParam(value = "logMin",defaultValue = "") String logMin,
                           @RequestParam(value = "logMax",defaultValue = "") String logMax,
                           @RequestParam(value = "unitName",defaultValue = "") String unitName,
                           @RequestParam(value = "code",defaultValue = "" ) String code){

        Map map = new HashMap();
        if(StringUtil.isNotNull(logMin)) {
            map.put("logMin", logMin);
        }
        if(StringUtil.isNotNull(logMax)) {
            map.put("logMax", logMax);
        }
        if(StringUtil.isNotNull(unitName)) {
            map.put("unitName", unitName);
        }
        if(StringUtil.isNotNull(code)) {
            map.put("code", code);
        }
        String num = licService.getTotal(map);

        return num;
    }

    @RequestMapping(value = "/list")
    public String listLic(Model model,
                          @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                          @RequestParam(value = "limit",defaultValue = "10") int limit,
                          @RequestParam(value = "searchKey",defaultValue = "") String searchKey){
        Map<String,Object> map = new HashMap();
        if(StrUtil.isNotEmpty(searchKey)){
            map.put("searchKey",searchKey);
        }else {
            map.put("searchKey",null);
        }
        Page<LicVo> page = PageHelper.startPage(pageNum,limit);
        List<LicVo> list = licService.listLic(map);

        model.addAttribute("total",page.getTotal());
        model.addAttribute("lics",list);

        return this.render("/lic-list::lic_list");
    }


    @ResponseBody
    @RequestMapping(value = "/saveLic")
    public RestResponseBo saveLic(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @ModelAttribute LicVo licVo){
        //校验参数
        LOGGER.info("开始生成并保存授权码..........");
        Map map = licService.insertLic(licVo);
        if(StringUtil.isNull(map)){
            LOGGER.error("生成并保存授权码失败");
            return RestResponseBo.fail(-1,"生成授权码失败，请刷新页面后重试");
        }
        if("1".equals(map.get("code"))){
            LOGGER.info(map.get("msg").toString());
            return RestResponseBo.ok(1,map.get("msg"));
        } else {
            LOGGER.error("生成授权码失败"+map.get("msg").toString());
            return RestResponseBo.fail(-1,map.get("msg").toString());
        }
    }

    @ResponseBody
    @RequestMapping(value = "/licDelete")
    public RestResponseBo licDelete(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value = "ids") String ids){

        Boolean bool = licService.deleteLicByIds(ids);
        if(bool){
            return RestResponseBo.ok(1,"删除成功");
        }else {
            return RestResponseBo.fail(-1,"删除失败，请刷新后重试！");
        }

    }

    @RequestMapping(value = "/downloadLicense")
    public void downloadLicense(HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam(value = "id") String id) throws IOException {
        licService.downloadLicense(response,id);
    }


}
