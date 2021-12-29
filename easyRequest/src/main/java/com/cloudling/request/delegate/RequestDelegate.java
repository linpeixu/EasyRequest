package com.cloudling.request.delegate;

import com.cloudling.request.network.BaseRequest;

/**
 * 描述: 请求代理
 * 联系：1966353889@qq.com
 * 日期: 2021/12/29
 */
public interface RequestDelegate {
    /**
     * 发起请求
     */
    <S, F> void request(BaseRequest<S, F> config);

    /**
     * 移除请求
     */
    <S, F> void remove(BaseRequest<S, F> config);

    /**
     * 通过tag移除请求
     */
    void removeByTag(String TAG);

    /**
     * 通过uuid移除请求
     */
    void removeByUUID(String uuid);
}
