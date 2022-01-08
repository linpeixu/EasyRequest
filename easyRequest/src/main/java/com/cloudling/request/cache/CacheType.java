package com.cloudling.request.cache;

/**
 * 描述: 缓存（或读取缓存）类型（设置什么时候需要缓存数据或读取缓存）
 * 联系: 1966353889@qq.com
 * 日期: 2022/1/7
 */
public enum CacheType {
    /**
     * 不缓存或不读取
     */
    NO,
    /**
     * 缓存成功（原始网络请求成功）的数据或读取成功（原始网络请求成功）的缓存数据
     */
    SOURCE_SUCCESS,
    /**
     * 缓存失败（原始网络请求失败）的数据或读取失败（原始网络请求失败）的缓存数据
     */
    SOURCE_FAIL,
}