package com.lwp.website.controller;

import com.lwp.website.config.SysConfig;
import com.lwp.website.entity.Bo.RestResponseBo;
import com.lwp.website.entity.Vo.UserVo;
import com.lwp.website.service.UserService;
import com.lwp.website.utils.RedisUtil;
import com.lwp.website.utils.StringUtil;
import com.lwp.website.utils.TaleUtils;
import com.lwp.website.utils.TipException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: liweipeng
 * @Date: 2020/06/27/14:40
 * @Description:
 */
@Controller(value = "indexController")
@RequestMapping("/")
@Transactional(rollbackFor = TipException.class)
public class IndexController extends BaseController{
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    @Resource
    private UserService userService;


    @Resource
    private RedisUtil redisUtil;

    @Autowired
    private SysConfig sysConfig;

    @RequestMapping(value = "/info")
    public String info(){
        return this.render("/info");
    }

    @ResponseBody
    @RequestMapping(value = "/savePwd")
    public RestResponseBo savePwd(@CookieValue("${website.defaultCookie}") String cookie,
                                  HttpServletRequest request,
                                  @RequestParam(value = "oldPwd") String oldPwd,
                                  @RequestParam(value = "newPwd") String newPwd,
                                  @RequestParam(value = "enPwd") String enPwd){
        UserVo userVo = TaleUtils.getLoginUser(request);
        Map map = userService.changePwd(oldPwd,newPwd,enPwd,userVo);
        //删除成功
        if("1".equals(map.get("code"))){
            //清除登录状态
            String loginUserKey = sysConfig.getLoginUser();
            try {
                redisUtil.del(loginUserKey+cookie);
                LOGGER.info("用户: "+userVo.getUsername()+"退出系统");
                return RestResponseBo.ok(1,map.get("msg"));
            }catch (Exception e){
                e.printStackTrace();
                return RestResponseBo.fail(-1,"修改密码成功，自动退出失败，请手动退出");
            }
        }
        else {
            return RestResponseBo.fail(-1,String.valueOf(map.get("msg")));
        }
    }

    @GetMapping(value = {"","/index"})
    public String index(Model model,
                        HttpServletRequest request,
                        HttpServletResponse response){
        UserVo userVo = TaleUtils.getLoginUser(request);
        model.addAttribute("user",userVo);
        return this.render("/index");
    }

    @GetMapping(value={"/getPage/{name}", "/getPage/{name}.html"})
    public String getHtml(HttpServletResponse response,@PathVariable String name){

        return this.render(name);
    }

    @GetMapping(value = "login")
    public String login(){
        return "/login";
    }

    @PostMapping(value = "login")
    @ResponseBody
    public RestResponseBo doLogin(@RequestParam String username,
                                  @RequestParam String password,
                                  @RequestParam (required = false) String rememberMe,
                                  @CookieValue("${website.defaultCookie}") String cookie,
                                  HttpServletRequest request,
                                  HttpServletResponse response){

        String loginErrorCountKey = sysConfig.getLoginErrorCount();
        String loginUserKey = sysConfig.getLoginUser();
        Integer error_count = StringUtil.isNull(redisUtil.get(loginErrorCountKey+cookie))?0:Integer.parseInt(redisUtil.get("login:e:c:"+cookie).toString());
        if(null != error_count && error_count >=3){
            return RestResponseBo.fail("您输入的密码已经错误超过3次，请10分钟后尝试");
        }else {
            try {
                UserVo user = userService.login(username,password);
                redisUtil.set(loginUserKey+cookie,user,60*30);
                if(StringUtils.isNotBlank(rememberMe)){
                    TaleUtils.setCookie(response,user.getId());
                }
                redisUtil.set(loginErrorCountKey+cookie,0,60*10);
                LOGGER.info("用户： "+username+"登录成功");
            }catch (Exception e){
                error_count = null == error_count ? 1 :error_count +1;
                if(error_count > 3){
                    return RestResponseBo.fail("您输入密码已经错误超过三次，请10分钟后尝试");
                }
                redisUtil.set(loginErrorCountKey+cookie,error_count,60*10);
                String msg = "登录失败";
                if(e instanceof TipException){
                    msg = e.getMessage();
                }
                e.printStackTrace();
                LOGGER.error(msg,e);
                return RestResponseBo.fail(msg);
            }
        }
        return RestResponseBo.ok();
    }

    @PostMapping(value = "/logout")
    @ResponseBody
    public RestResponseBo logout(@CookieValue("${website.defaultCookie}") String cookie,
                                 HttpServletRequest request,
                                 HttpServletResponse response){
        String loginUserKey = sysConfig.getLoginUser();
        try {
            UserVo userVo = TaleUtils.getLoginUserByRedis(request);
            redisUtil.del(loginUserKey+cookie);
            LOGGER.info("用户: "+userVo.getUsername()+"退出系统");
            return RestResponseBo.ok();
        }catch (Exception e){
            e.printStackTrace();
            return RestResponseBo.fail();
        }
    }
}
