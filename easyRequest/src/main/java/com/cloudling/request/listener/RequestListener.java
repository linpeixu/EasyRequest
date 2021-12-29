package com.cloudling.request.listener;

/**
 * 描述: 请求结果监听
 * 联系：1966353889@qq.com
 * 日期: 2021/12/29
 */
public interface RequestListener {
    /**
     * 成功
     *
     * @param result 回调参数
     */
    void onSuccess(String result);

    /**
     * 失败
     *
     * @param result 回调参数
     */
    void onFail(String result);
}
