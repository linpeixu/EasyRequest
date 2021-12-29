package com.cloudling.request.listener;

/**
 * 描述: 请求结果监听（带请求开始和结束回调）
 * 联系：1966353889@qq.com
 * 日期: 2021/12/29
 */
public interface ConverterListener<S, F> {
    /**
     * 成功
     *
     * @param result 回调参数
     */
    void onSuccess(S result);

    /**
     * 失败
     *
     * @param result 回调参数
     */
    void onFail(F result);
}
