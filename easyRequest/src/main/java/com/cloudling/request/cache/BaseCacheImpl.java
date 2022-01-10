package com.cloudling.request.cache;

import android.content.Context;

import com.cloudling.request.type.OriginalCallback;

/**
 * 描述: 缓存实现约束基类
 * 联系: 1966353889@qq.com
 * 日期: 2022/1/6
 */
public abstract class BaseCacheImpl {
    /**
     * 默认保存时长，单位为毫秒（默认值60秒）
     */
    protected final long mDefaultDuration = 60 * 1000;
    /**
     * 上下文
     */
    protected Context mContext;
    /**
     * 最大缓存数量，默认20个
     */
    protected int mMaxCacheSize = 20;

    public BaseCacheImpl(Context context) {
        mContext = context;
    }

    public BaseCacheImpl(Context context, int maxCacheSize) {
        mContext = context;
        mMaxCacheSize = maxCacheSize;
    }

    /**
     * 自动清理
     */
    public abstract void autoClean();

    /**
     * 新增缓存
     *
     * @param key   缓存内容的key
     * @param value 缓存内容
     */
    public abstract void addCache(String key, String value);

    /**
     * 新增缓存
     *
     * @param key      缓存内容的key
     * @param value    缓存内容
     * @param duration 缓存时长
     */
    public abstract void addCache(String key, String value, long duration);

    /**
     * 新增缓存
     *
     * @param key      缓存内容的key
     * @param value    缓存内容
     * @param duration 缓存时长
     * @param timeUnit 时长单位
     */
    public abstract void addCache(String key, String value, long duration, TimeUnit timeUnit);

    /**
     * 获取缓存
     *
     * @param key 缓存内容的key
     */
    public abstract String getCache(String key);

    /**
     * 移除缓存
     *
     * @param key 缓存内容的key
     */
    public abstract void removeCache(String key);

    /**
     * 清除全部缓存
     */
    public abstract void removeAllCache();
}
