package com.cloudling.request.network;

/**
 * 描述: 默认请求回调参数为String的Builder
 * 联系：1966353889@qq.com
 * 日期: 2021/12/29
 */
public final class DefaultRequest {
    public static BaseRequest.Builder<String, String> create() {
        return new BaseRequest.Builder<String, String>();
    }
}
