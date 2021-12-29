package com.cloudling.request.listener;

/**
 * 描述: 请求结果监听（带请求开始和结束回调）
 * 联系：1966353889@qq.com
 * 日期: 2021/12/29
 */
public interface WholeConverterListener<S, F> extends ConverterListener<S, F> {
    /**
     * 请求开始回调
     */
    void requestBefore();

    /**
     * 请求结束回调
     */
    void requestAfter();
}
