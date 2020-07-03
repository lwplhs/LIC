package com.lwp.website.service.impl;

import com.lwp.website.config.SysConfig;
import com.lwp.website.dao.LicDao;
import com.lwp.website.entity.Vo.LicVo;
import com.lwp.website.service.LicService;
import com.lwp.website.utils.*;
import com.lwp.website.utils.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import org.apache.tools.zip.ZipOutputStream;
import org.apache.tools.zip.ZipEntry;
import sun.nio.cs.ext.GBK;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: liweipeng
 * @Date: 2020/06/28/16:07
 * @Description:
 */
@Service
public class LicServiceImpl implements LicService {

    private static Logger LOGGER = LoggerFactory.getLogger(LicServiceImpl.class);

    @Autowired
    private SysConfig sysConfig;

    @Resource
    private LicDao licDao;

    @Override
    public List<LicVo> listLic(Map map) {
        List<LicVo> list = licDao.selectListLic(map);
        return list;
    }

    @Override
    public Map insertLic(LicVo licVo) {
        Map<String,String> map = new HashMap();
        map = validLicVo(licVo,map);
        if(StringUtil.isNull(map) || !"8".equals(map.get("code"))){
            map.put("code","-1");
            return map;
        }
        if(StringUtil.isNull(licVo.getLicCode())){
            licVo.setLicCode(UUID.UU64());
        }
        licVo.setId(UUID.createID());
        licVo.setORA(new Date());
        licVo.setIsDelete("0");
        int flag = 0;
        try {
            String license = RSA.getLicense(licVo);
            licVo.setLicense(license);
            flag = licDao.insertLic(licVo);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(flag > 0){
            map.put("code","1");
            map.put("msg","生成授权码成功");
        }else {
            map.put("code","-1");
            map.put("msg","生成授权码失败，请刷新页面后重试");
        }
        return map;
    }

    @Override
    public Boolean deleteLicByIds(String ids) {
        List<String> temp = this.toList(ids);
        if(!StringUtil.isNull(temp)){
            Map<String,Object> map = new HashMap<>();
            map.put("ids",temp);
            int count = licDao.deleteLicByIds(map);
            if(count > 0){
                return true;
            }
        }
        return false;
    }

    @Override
    public void downloadLicense(HttpServletResponse response, String id) throws IOException {
        if(StringUtil.isNull(id)){
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/html; charset=utf-8");
            PrintWriter out = response.getWriter();
            out.print("<script type=\"text/javascript\">alert('文件不存');window.history.back(-1);</script>");
        }
        else {
            response.setContentType("application/octet-stream");
            LicVo licVo = licDao.selectLicById(id);
            String zipName = licVo.getUnitName()+System.currentTimeMillis()+".zip";
            zipName = java.net.URLEncoder.encode(zipName, "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename="+zipName);
            ZipOutputStream zos = null;
            try{
                zos = new ZipOutputStream(response.getOutputStream());
                File file = this.creatLicenseFile(licVo);
                this.compress(file,zos,file.getName(),true);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    if (null != zos){
                        zos.flush();
                        zos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 递归压缩方法
     * @param sourceFile 源文件
     * @param zos        zip输出流
     * @param name       压缩后的名称
     * @param KeepDirStructure  是否保留原来的目录结构,true:保留目录结构;
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */
    private void compress(File sourceFile, ZipOutputStream zos, String name,
                                 boolean KeepDirStructure) throws Exception{
        byte[] buf = new byte[1024];
        if(sourceFile.isFile()){
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1){
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            //是文件夹
            File[] listFiles = sourceFile.listFiles();
            if(listFiles == null || listFiles.length == 0){
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if(KeepDirStructure){
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }

            }else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (KeepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + "/" + file.getName(),KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(),KeepDirStructure);
                    }

                }
            }
        }
    }

    private File creatLicenseFile(LicVo licVo) throws Exception{
        String path = TaleUtils.getLicensePath();
        File file = new File(path+"/sn");
        //获取父目录
        File fileParent = file.getParentFile();
        //判断是否存在
        if (!fileParent.exists()) {
            //创建父目录文件
            fileParent.mkdirs();
        }
        if(file.exists()){
            file.delete();
        }
        FileOutputStream fos = new FileOutputStream(file);
        String license = licVo.getLicense();
        license = AesUtil.deAes(license,sysConfig.getAesSalt());
        byte[] data = license.getBytes();
        fos.write(data,0,data.length);
        fos.flush();
        fos.close();
        return file;
    }

    /**
     * 校验参数
     * @param licVo
     * @param map
     * @return
     */
    private Map validLicVo(LicVo licVo,Map map){
        if(StringUtil.isNull(licVo)){
            map.put("code","0");
            map.put("msg","licVo为空");
        }else{
            String unitName = licVo.getUnitName();
            String isMac = licVo.getIsMac();
            Date ex = licVo.getEx();
            String mac = licVo.getMac();
            String concurrent =  licVo.getConcurrent().trim();
            if(StringUtil.isNull(unitName)){
                map.put("code","1");
                map.put("msg","用户名称不能为空");
            }else if(StringUtil.isNull(isMac)){
                map.put("code","2");
                map.put("msg","请选择是否需要校验mac地址");
            }else if(isMac.equals("1") && StringUtil.isNull(mac)){
                map.put("code","3");
                map.put("msg","MAC地址不能为空");
            }else if(StringUtil.isNull(concurrent)){
                map.put("code","4");
                map.put("msg","用户并发数不能为空");
            }else if(!StringUtil.isPositiveInteger(concurrent)){
                map.put("code","5");
                map.put("msg","用户并发数只能输入数字");
            }else if(StringUtil.isNull(ex)){
                map.put("code","6");
                map.put("msg","到期时间不能为空");
            }else if(ex.before(new Date())){
                map.put("code","7");
                map.put("msg","到期时间请选择今天以后的时间");
            }else {
                map.put("code","8");
            }
        }
        return map;
    }

    /**
     * 将 ids 转化成list
     * @param ids
     * @return
     */
    private List<String> toList(String ids){
        List<String> list = new ArrayList();
        String[] args = ids.split(",");
        if(null != args && args.length > 0){
            for(int i = 0;i < args.length;i++){
                list.add(args[i]);
            }
        }
        return list;
    }
}
