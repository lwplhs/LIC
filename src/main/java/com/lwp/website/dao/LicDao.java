package com.lwp.website.dao;

import com.lwp.website.entity.Vo.LicVo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: liweipeng
 * @Date: 2020/06/28/14:51
 * @Description:
 */
@Component
public interface LicDao {


    /**
     * 增加授权信息
     * @param licVo
     * @return
     */
    int insertLic(LicVo licVo);

    /**
     * 增加授权码
     */
    int insertLicense(String id,String license);

    /**
     * 查询列表
     * @return
     */
    List<LicVo> selectListLic(Map map);

    /**
     * 根据id 查询列表
     * @param id
     * @return
     */
    LicVo selectLicById(String id);

    int deleteLicByIds(Map<String,Object> map);

}
