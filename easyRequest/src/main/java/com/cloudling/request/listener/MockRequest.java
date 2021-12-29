package com.cloudling.request.listener;

import com.cloudling.request.type.MockRequestCallbackType;

/**
 * 描述: 模拟请求配置
 * 联系：1966353889@qq.com
 * 日期: 2021/12/29
 */
public interface MockRequest {
    /**
     * 请求时长
     */
    int requestDuration();

    /**
     * 模拟请求结果回调类型
     */
    MockRequestCallbackType callbackType();

    /**
     * 模拟请求结果返回值
     */
    String result();
}
