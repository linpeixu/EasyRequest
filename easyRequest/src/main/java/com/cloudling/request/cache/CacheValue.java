package com.cloudling.request.cache;

/**
 * 描述:
 * 作者: lpx
 * 日期: 2022/1/6
 */
@Deprecated
public class CacheValue {
    /**
     * 写入时当前时间
     */
    private final long writeTimeMillis;
    /**
     * 缓存的时长
     */
    private final long saveDuration;
    /**
     * 缓存的内容
     */
    private final String value;

    public CacheValue(long saveDuration, String value) {
        writeTimeMillis = System.currentTimeMillis();
        this.saveDuration = saveDuration;
        this.value = value;
    }
}
