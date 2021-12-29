package com.cloudling.request.type;

/**
 * 描述: 请求结果转换回调类型（可强制将请求结果回调为成功，失败）
 * 联系：1966353889@qq.com
 * 日期: 2021/12/29
 */
public enum TransformCallbackType {
    /**
     * 成功
     */
    SUCCESS,
    /**
     * 失败
     */
    FAIL,
    /**
     * 默认（跟随网络请求回调）
     */
    DEFAULT
}
