package com.cloudling.request.cache;

/**
 * 描述: 读取缓存类型（设置什么时候需要读取什么样的缓存数据）
 * 联系: 1966353889@qq.com
 * 日期: 2022/1/7
 */
public enum ReadCacheType {
    /**
     * 不读取
     */
    NO,
    /**
     * 默认（跟随网络请求回调成功或失败读取对应类型的缓存数据）
     */
    DEFAULT,
    /**
     * 当前网络请求成功时读取成功（原始网络请求成功）的缓存数据
     */
    READ_SUCCESS_AFTER_SUCCESS,
    /**
     * 当网络请求成功时读取失败（原始网络请求失败）的缓存数据
     */
    READ_FAIL_AFTER_SUCCESS,
    /**
     * 当前网络请求失败时读取成功（原始网络请求成功）的缓存数据
     */
    READ_SUCCESS_AFTER_FAIL,
    /**
     * 当网络请求失败时读取失败（原始网络请求失败）的缓存数据
     */
    READ_FAIL_AFTER_FAIL
}
