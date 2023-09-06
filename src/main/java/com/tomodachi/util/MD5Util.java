package com.tomodachi.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

/**
 * MD5 工具类
 */
@Component
public class MD5Util {
    @Value("${momo-companion.md5-salt}")
    private String salt;

    /**
     * MD5 加密
     *
     * @param str 待加密字符串
     * @return 加密后字符串
     */
    public String encrypt(String str) {
        String encryptedStr = DigestUtils.md5DigestAsHex(str.getBytes());
        encryptedStr = DigestUtils.md5DigestAsHex((encryptedStr + salt).getBytes());
        return encryptedStr;
    }
}
