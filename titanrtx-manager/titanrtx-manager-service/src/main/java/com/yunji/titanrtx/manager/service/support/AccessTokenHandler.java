package com.yunji.titanrtx.manager.service.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 简化告警链接访问，不需要登录。
 *
 * @author leihz
 * @since 2020-05-14 3:13 下午
 */
@Component
@Slf4j
public class AccessTokenHandler {
    private static final long TWO_DAYS_STAMPS = 48 * 60 * 60 * 1000;

    /**
     * 2020-06-10-0358-%E5%8E%8B%E6%B5%8B%E8%AE%B0%E5%BD%95.docx not match.
     */
    public static boolean canUrlAccess(String key) {
        try {
            String decodeKey = URLDecoder.decode(key, "UTF-8");

            String value = CacheManager.get(decodeKey);
            //2天时间
            if (value != null && System.currentTimeMillis() - Long.parseLong(value) < TWO_DAYS_STAMPS) {
                log.info("Url {} can access to titan.", key);
                return true;
            } else {
                log.warn("Key:{} not match.", key);
            }
        } catch (UnsupportedEncodingException ignored) {
        }
        return false;
    }


}
