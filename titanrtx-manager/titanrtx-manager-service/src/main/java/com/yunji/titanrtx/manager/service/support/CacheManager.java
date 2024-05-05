package com.yunji.titanrtx.manager.service.support;

import com.google.common.cache.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * CacheManager
 *
 * @author leihz
 * @since 2020-05-14 3:21 下午
 */
@Slf4j
public class CacheManager {
    /**
     * 缓存项最大数量
     */
    private static final long GUAVA_CACHE_SIZE = 1000;

    /**
     * 缓存时间：天
     */
    private static final long GUAVA_CACHE_DAY = 7;

    /**
     * 缓存操作对象
     */
    private static LoadingCache<String, String> GLOBAL_CACHE = null;

    static {
        try {
            GLOBAL_CACHE = loadCache(new CacheLoader<String, String>() {
                // 处理缓存键不存在缓存值时的处理逻辑
                @Override
                public String load(String key) throws Exception {
                    return 0 + "";
                }
            });
        } catch (Exception e) {
            log.error("初始化Guava Cache出错", e);
        }
    }

    /**
     * 全局缓存设置
     */
    private static LoadingCache<String, String> loadCache(CacheLoader<String, String> cacheLoader) throws Exception {
        LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                //缓存池大小，在缓存项接近该大小时， Guava开始回收旧的缓存项
                .maximumSize(GUAVA_CACHE_SIZE)
                //设置时间对象没有被读/写访问则对象从内存中删除(在另外的线程里面不定期维护)
                .expireAfterAccess(GUAVA_CACHE_DAY, TimeUnit.DAYS)
                // 设置缓存在写入之后 设定时间 后失效
                .expireAfterWrite(GUAVA_CACHE_DAY, TimeUnit.DAYS)
                //移除监听器,缓存项被移除时会触发
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, String> rn) {
                        log.info("Remove guava cache key:{}, value:{}, cause:{}.", rn.getKey(), rn.getValue(), rn.getCause());
                    }
                })
                //开启Guava Cache的统计功能
                .recordStats()
                .build(cacheLoader);

        log.info("Create guava cache ok, {}.", cache);
        return cache;
    }

    /**
     * 设置缓存值
     * 注: 若已有该key值，则会先移除(会触发removalListener移除监听器)，再添加
     */
    public static void put(String key, String value) {
        try {
            GLOBAL_CACHE.put(key, value);
            log.info("Put key:{},value:{} ok.", key, value);
        } catch (Exception e) {
            log.error("设置缓存值出错", e);
        }
    }

    /**
     * 批量设置缓存值
     */
    public static void putAll(Map<? extends String, ? extends String> map) {
        try {
            GLOBAL_CACHE.putAll(map);
        } catch (Exception e) {
            log.error("批量设置缓存值出错", e);
        }
    }

    /**
     * 获取缓存值
     * 注：如果键不存在值，将调用CacheLoader的load方法加载新值到该键中
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        String token = "";
        try {
            token = GLOBAL_CACHE.get(key);
        } catch (Exception e) {
            log.error("获取缓存值出错", e);
        }
        return token;
    }

    /**
     * 移除缓存
     *
     * @param key
     */
    public static void remove(Long key) {
        try {
            GLOBAL_CACHE.invalidate(key);
        } catch (Exception e) {
            log.error("移除缓存出错", e);
        }
    }

    /**
     * 批量移除缓存
     *
     * @param keys
     */
    public static void removeAll(Iterable<Long> keys) {
        try {
            GLOBAL_CACHE.invalidateAll(keys);
        } catch (Exception e) {
            log.error("批量移除缓存出错", e);
        }
    }

    /**
     * 清空所有缓存
     */
    public static void removeAll() {
        try {
            GLOBAL_CACHE.invalidateAll();
        } catch (Exception e) {
            log.error("清空所有缓存出错", e);
        }
    }

    /**
     * 获取缓存项数量
     */
    public static long size() {
        long size = 0;
        try {
            size = GLOBAL_CACHE.size();
        } catch (Exception e) {
            log.error("获取缓存项数量出错", e);
        }
        return size;
    }
}