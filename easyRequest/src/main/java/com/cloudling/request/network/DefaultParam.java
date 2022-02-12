package com.cloudling.request.network;

import java.util.LinkedHashMap;

/**
 * 描述: 创建默认的构建请求参数
 * 联系：1966353889@qq.com
 * 日期: 2022/2/12
 */
public class DefaultParam extends RequestParam<LinkedHashMap<String, Object>> {
    @Override
    public LinkedHashMap<String, Object> initParam() {
        return new LinkedHashMap<>();
    }
}
