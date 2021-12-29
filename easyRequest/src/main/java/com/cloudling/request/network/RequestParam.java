package com.cloudling.request.network;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述: 返回构建请求参数
 * 联系：1966353889@qq.com
 * 日期: 2021/12/29
 */
public class RequestParam {
    private Map<String, Object> param;

    public RequestParam() {
        param = new HashMap<>();
    }

    public RequestParam put(String key, Object value) {
        param.put(key, value);
        return this;
    }

    public Map<String, Object> build() {
        return param;
    }
}
