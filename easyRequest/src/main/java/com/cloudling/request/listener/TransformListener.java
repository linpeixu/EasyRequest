package com.cloudling.request.listener;

import com.cloudling.request.type.TransformCallbackType;

/**
 * 描述: 请求结果转换
 * 联系：1966353889@qq.com
 * 日期: 2021/12/29
 */
public interface TransformListener {
    /**
     * 转换请求结果（可对请求回调参数做包装）
     *
     * @param result 回调参数
     */
    String onTransformResult(String result);

    /**
     * 是否将结果转成成功
     */
    TransformCallbackType callbackType();
}
