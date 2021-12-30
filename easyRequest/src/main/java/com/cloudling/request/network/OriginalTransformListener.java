package com.cloudling.request.network;

import com.cloudling.request.listener.TransformListener;
import com.cloudling.request.type.OriginalCallback;

/**
 * 描述: 请求结果转换（带原始的回调类型（网络请求回调成功或失败））
 * 联系：1966353889@qq.com
 * 日期: 2021/12/30
 */
public abstract class OriginalTransformListener implements TransformListener {
    public abstract String onTransformResult(OriginalCallback type, String result);

    @Override
    public final String onTransformResult(String result) {
        return null;
    }
}
