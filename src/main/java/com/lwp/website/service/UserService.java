package com.lwp.website.service;

import com.lwp.website.entity.Vo.UserVo;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: liweipeng
 * @Date: 2020/06/27/12:39
 * @Description:
 */
public interface UserService {

    /**
     * 保存用户数据
     * @param userVo
     * @return
     */
    Integer insertUser(UserVo userVo);

    /**
     * 通过uid查找对象
     * @param uid
     * @return
     */
    UserVo queryUserById(String uid);

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    UserVo login(String username, String password);

    /**
     * 根据主键更新user对象
     * @param userVo
     */
    void updateByUid(UserVo userVo);

    Map changePwd(String oldPwd, String newPwd, String enPwd,UserVo userVo);
}
