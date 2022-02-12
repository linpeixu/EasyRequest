package com.cloudling.request.network;

import java.util.Map;

/**
 * 描述: 返回构建请求参数
 * 联系：1966353889@qq.com
 * 日期: 2021/12/29
 */
public abstract class RequestParam<MapType extends Map<String, Object>> {
    private final MapType param;

    public RequestParam() {
        param = initParam();
    }

    public abstract MapType initParam();

    public RequestParam<MapType> put(String key, Object value) {
        param.put(key, value);
        return this;
    }

    public MapType build() {
        return param;
    }
}
