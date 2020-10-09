package com.lwp.website.dao;

import com.lwp.website.entity.Vo.UserVo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: liweipeng
 * @Date: 2020/06/27/14:10
 * @Description:
 */
@Component
public interface UserDao {

    List<UserVo> selectByUser(UserVo userVo);

    int updatePwd(String id,String pwd);


    UserVo selectUserByName(String userName);

}
