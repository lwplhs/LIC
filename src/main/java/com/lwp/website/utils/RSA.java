package com.lwp.website.utils;

import com.alibaba.fastjson.JSONObject;
import com.lwp.website.config.SysConfig;
import com.lwp.website.entity.Vo.LicVo;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: ${description}
 * @author: dfz
 * @create: 2019-05-16
 **/
@Component
public class RSA {


    private static SysConfig sysConfig;

    @Autowired
    public void setSysConfig(SysConfig sysConfig){
        RSA.sysConfig = sysConfig;
    }

    private static final String KEY_ALGORITHM = "RSA";
    private static final int RSA_KEY_SIZE = 1024;
    public final static String UTF8 = "utf-8";
    /** *//**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /** *//**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    private static Map<Integer, String> keyMap = new HashMap<Integer, String>();


    public static String formatString(String source) {
        if (source == null) {
            return null;
        }
        return source.replaceAll("\\r", "").replaceAll("\\n", "");
    }


    /**
     * md5加密
     *
     * @param source 数据源
     * @return 加密字符串
     */
    public static String MD5encode(String source) {
        if (null == source && "".equals(source))  {
            return null;
        }
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ignored) {
        }
        byte[] encode = messageDigest.digest(source.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte anEncode : encode) {
            String hex = Integer.toHexString(0xff & anEncode);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String getLicense(LicVo licVo){

        String isMac = licVo.getIsMac();

        //
        Date date = licVo.getEx();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String ex = format.format(date);
        //
        String name = licVo.getUnitName();
        //
        String concurrent = licVo.getConcurrent();
        //
        String code = licVo.getLicCode();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name",name);
        jsonObject.put("ex",ex);
        jsonObject.put("concurrent",concurrent);
        jsonObject.put("code",code);
        if("1".equals(isMac)){
            String mac = licVo.getMac();
            mac = MD5encode(mac.toLowerCase());
            jsonObject.put("isMac",true);
            jsonObject.put("mac",mac);
        }else {
            jsonObject.put("isMac",false);
        }
        String priKey = sysConfig.getPriKey();
        String enc = encryptByPrivateKey(jsonObject.toString(), priKey);
        try {
            enc = AesUtil.enAes(enc,sysConfig.getAesSalt());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return enc;
    }

    /**
     * 随机生成密钥对
     *
     * @throws NoSuchAlgorithmException
     */
    public static void genKeyPair() throws NoSuchAlgorithmException {
        //KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        //初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(RSA_KEY_SIZE, new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        // 得到私钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        // 得到公钥
        String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));
        // 得到私钥字符串
        String privateKeyString = new String(Base64.encodeBase64((privateKey.getEncoded())));
        // 将公钥和私钥保存到Map
        keyMap.put(0, publicKeyString);// 0表示公钥
        keyMap.put(1, privateKeyString);// 1表示私钥
    }

    /**
     * RSA公钥加密
     *
     * @param str       加密字符串
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */

    public static String encrypt(String str, String publicKey) throws Exception {
        publicKey = formatString(publicKey);
        //base64编码的公钥
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] encryptData = str.getBytes(UTF8);
        int inputLen = encryptData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        //分段加密
        while(inputLen - offSet > 0){
            if(inputLen - offSet > MAX_ENCRYPT_BLOCK){
                cache = cipher.doFinal(encryptData,offSet,MAX_ENCRYPT_BLOCK);
            }else {
                cache = cipher.doFinal(encryptData,offSet,inputLen-offSet);
            }
            out.write(cache,0,cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }

        String outStr = Base64.encodeBase64String(out.toByteArray());
        return outStr;
    }

    /**
     * RSA私钥解密
     *
     * @param str        加密字符串
     * @param privateKey 私钥
     * @return 铭文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decrypt(String str, String privateKey) throws Exception {
        privateKey = formatString(privateKey);
        //64位解码加密后的字符串
        byte[] inputByte = Base64.decodeBase64(str.getBytes(UTF8));
        // base64编码的私钥
        byte[] decoded = Base64.decodeBase64(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(decoded));
        // RSA解密
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, priKey);

        int inputLen = inputByte.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        //对数据分段解密
        while (inputLen - offSet > 0){
            if(inputLen - offSet >MAX_DECRYPT_BLOCK){
                cache = cipher.doFinal(inputByte,offSet,MAX_DECRYPT_BLOCK);
            }else {
                cache = cipher.doFinal(inputByte,offSet,inputLen-offSet);
            }
            out.write(cache,0,cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        String outStr = new String(out.toByteArray());
        return outStr;
    }

    /**
     * 私钥加密
     *
     * @param data       加密数据
     * @param privateKey 私钥
     * @return
     */
    public static String encryptByPrivateKey(String data, String privateKey) {
        try {
            privateKey = formatString(privateKey);
            byte[] keyBytes = Base64.decodeBase64(privateKey.getBytes(UTF8));
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey key = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptData = data.getBytes(UTF8);
            int inputLen = encryptData.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0 ;
            byte[] cache;
            int i = 0;
            //对数据分段加密
            while(inputLen - offSet > 0){
                if(inputLen - offSet > MAX_ENCRYPT_BLOCK){
                    cache = cipher.doFinal(encryptData,offSet,MAX_ENCRYPT_BLOCK);
                }else {
                    cache = cipher.doFinal(encryptData,offSet,inputLen-offSet);
                }
                out.write(cache,0,cache.length);
                i++;
                offSet = i * MAX_ENCRYPT_BLOCK;
            }
/*            byte[] b = data.getBytes(UTF8);cipher.doFinal(b)*/
            byte[] encrypt = out.toByteArray();
            return Base64.encodeBase64String(encrypt);
        } catch (Exception e) {
            //logger.error("Failed to encryptByPrivateKey data.", e);
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * 公钥解密
     *
     * @param data      解密数据
     * @param publicKey 公钥
     * @return
     */
    public static String decryptByPublicKey(String data, String publicKey) {
        try {
            publicKey = formatString(publicKey);
            byte[] kb = Base64.decodeBase64(publicKey.getBytes(UTF8));
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(kb);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey key = keyFactory.generatePublic(x509EncodedKeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            //Cipher cipher = Cipher.getInstance(RSA_PADDING_KEY);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] b = data.getBytes(UTF8);
            byte[] encryptedData = Base64.decodeBase64(b);
            int inputLen = encryptedData.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cashe;
            int i = 0;
            //对数据分段加密
            while (inputLen - offSet > 0){
                if(inputLen - offSet > MAX_DECRYPT_BLOCK){
                    cashe = cipher.doFinal(encryptedData,offSet,MAX_DECRYPT_BLOCK);
                }else {
                    cashe = cipher.doFinal(encryptedData,offSet,inputLen - offSet);
                }
                out.write(cashe,0,cashe.length);
                i++;
                offSet = i * MAX_DECRYPT_BLOCK;
            }
            //cipher.doFinal(Base64.decodeBase64(b))
            //Base64.decodeBase64(b)
            byte[] decrypt = out.toByteArray();
            out.close();
            return new String(decrypt, UTF8);
        } catch (Exception e) {
            //logger.error("Failed to decryptByPublicKey data.", e);
            throw new RuntimeException();
        }
    }
}
