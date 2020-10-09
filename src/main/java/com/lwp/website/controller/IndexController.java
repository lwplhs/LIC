package com.lwp.website.controller;

import com.lwp.website.entity.Bo.RestResponseBo;
import com.lwp.website.entity.Vo.UserVo;
import com.lwp.website.exception.TipException;
import com.lwp.website.service.UserService;
import com.lwp.website.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @RequestMapping(value = "/info")
    public String info(){
        return this.render("/info");
    }

    @ResponseBody
    @RequestMapping(value = "/savePwd")
    public RestResponseBo savePwd(HttpServletRequest request,
                                  @RequestParam(value = "oldPwd") String oldPwd,
                                  @RequestParam(value = "newPwd") String newPwd,
                                  @RequestParam(value = "enPwd") String enPwd){
        UserVo userVo = (UserVo) SecurityUtils.getSubject().getPrincipal();//使用shiro进行管理//TaleUtils.getLoginUser(request);
        Map map = userService.changePwd(oldPwd,newPwd,enPwd,userVo);
        //删除成功
        if("1".equals(map.get("code"))){
            //清除登录状态
            try {
                //UserRedisUtil.delUserSession(request);
                SecurityUtils.getSubject().logout();
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
        UserVo userVo = (UserVo) SecurityUtils.getSubject().getPrincipal();//修改使用Shiro//TaleUtils.getLoginUser(request);
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
                                  HttpServletRequest request,
                                  HttpServletResponse response){
        /*Integer error_count = UserRedisUtil.getErrorCount(request);
        if(null != error_count && error_count >=3){
            return RestResponseBo.fail("您输入的密码已经错误超过3次，请10分钟后尝试");
        }else {
            try {
                UserVo user = userService.login(username,password);
                UserRedisUtil.insertOrUpdateUserSession(request,user);
                if(StringUtils.isNotBlank(rememberMe)){
                    TaleUtils.setCookie(response,user.getId());
                }
                UserRedisUtil.insertErrorCount(request,error_count);
                UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username,username+password);
                // 进行验证，这里可以捕获异常，然后返回对应信息
                SecurityUtils.getSubject().login(usernamePasswordToken);

                LOGGER.info("用户： "+username+"登录成功");
            }catch (Exception e){
                error_count = null == error_count ? 1 :error_count +1;
                if(error_count > 3){
                    return RestResponseBo.fail("您输入密码已经错误超过三次，请10分钟后尝试");
                }
                UserRedisUtil.insertErrorCount(request,error_count);
                String msg = "登录失败";
                if(e instanceof TipException){
                    msg = e.getMessage();
                }
                e.printStackTrace();
                LOGGER.error(msg,e);
                return RestResponseBo.fail(msg);
            }
        }*/
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username,username+password);
        // 进行验证，这里可以捕获异常，然后返回对应信息
        try {
            SecurityUtils.getSubject().login(usernamePasswordToken);
            LOGGER.info(username + "  登录成功");
        }catch (Exception e){
            LOGGER.info("用户名："+username+"  密码："+password+"   登录失败，");
            throw new UnknownAccountException();
        }

        return RestResponseBo.ok();
    }

    @PostMapping(value = "/logout")
    @ResponseBody
    public RestResponseBo logout(/*@CookieValue("${website.defaultCookie}") String cookie,*/
                                 HttpServletRequest request,
                                 HttpServletResponse response){
        try {
            UserVo userVo = (UserVo) SecurityUtils.getSubject().getPrincipal();//修改使用shiro//TaleUtils.getLoginUserByRedis(request);
            SecurityUtils.getSubject().logout();//修改使用shiro//UserRedisUtil.delUserSession(request);
            LOGGER.info("用户: "+userVo.getUsername()+"退出系统");
            return RestResponseBo.ok();
        }catch (Exception e){
            e.printStackTrace();
            return RestResponseBo.fail();
        }
    }
}
