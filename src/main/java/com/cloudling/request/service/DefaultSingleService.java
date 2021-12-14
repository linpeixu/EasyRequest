package com.cloudling.request.service;

import android.util.ArrayMap;

import com.cloudling.request.service.base.BaseService;

/**
 * 描述: 单服务接口默认请求服务基类（已实现默认超时时间，不同环境继承该类设置不同的host）
 * 联系：1966353889@qq.com
 * 日期: 2021/12/3
 */
public abstract class DefaultSingleService extends BaseService {
    public DefaultSingleService() {
        super();
    }

    @Override
    public final ArrayMap<String, String> setMicroConfig() {
        return null;
    }

    @Override
    public final long connectTimeout() {
        return 20;
    }

    @Override
    public final long readTimeout() {
        return 20;
    }

    @Override
    public final long writeTimeout() {
        return 20;
    }
}
