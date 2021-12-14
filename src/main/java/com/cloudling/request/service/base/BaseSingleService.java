package com.cloudling.request.service.base;

import android.util.ArrayMap;

/**
 * 描述: 单服务接口请求服务基类（不同环境继承该类设置不同的host）
 * 联系：1966353889@qq.com
 * 日期: 2021/12/3
 */
public abstract class BaseSingleService extends BaseService {
    public BaseSingleService() {
        super();
    }

    @Override
    public final ArrayMap<String, String> setMicroConfig() {
        return null;
    }

}
