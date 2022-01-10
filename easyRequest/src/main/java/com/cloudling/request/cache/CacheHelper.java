package com.cloudling.request.cache;

import android.content.Context;

import com.cloudling.request.type.OriginalCallback;

/**
 * 描述: 缓存助手
 * 联系: 1966353889@qq.com
 * 日期: 2022/1/6
 */
public final class CacheHelper {
    private Context mContext;
    private static CacheHelper mInstance;
    private BaseCacheImpl mBaseCacheImpl;

    private CacheHelper() {
    }

    public static CacheHelper getInstance() {
        if (mInstance == null) {
            synchronized (CacheHelper.class) {
                if (mInstance == null) {
                    mInstance = new CacheHelper();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
            mBaseCacheImpl = new DefaultCacheImpl(mContext);
        }
    }

    public void init(Context context, BaseCacheImpl baseCacheImpl) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
            mBaseCacheImpl = baseCacheImpl;
        }
    }

    public void addCache(String key, String value) {
        if (mBaseCacheImpl != null) {
            mBaseCacheImpl.addCache(key, value);
        }
    }

    public void addCache(String key, String value, long duration) {
        if (mBaseCacheImpl != null) {
            mBaseCacheImpl.addCache(key, value, duration);
        }
    }

    public void addCache(String key, String value, long duration, TimeUnit timeUnit) {
        if (mBaseCacheImpl != null) {
            mBaseCacheImpl.addCache(key, value, duration, timeUnit);
        }
    }

    public String getCache(String key) {
        if (mBaseCacheImpl != null) {
            return mBaseCacheImpl.getCache(key);
        }
        return null;
    }

    public void removeCache(String key) {
        if (mBaseCacheImpl != null) {
            mBaseCacheImpl.removeCache(key);
        }
    }

    public void removeAllCache() {
        if (mBaseCacheImpl != null) {
            mBaseCacheImpl.removeAllCache();
        }
    }
}
