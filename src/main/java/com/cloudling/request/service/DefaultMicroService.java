package com.cloudling.request.service;

import com.cloudling.request.service.base.BaseService;

/**
 * 描述: 微服务接口默认请求服务基类（已实现默认超时时间，不同环境继承该类设置不同的host）
 * 联系：1966353889@qq.com
 * 日期: 2021/12/3
 */
public abstract class DefaultMicroService extends BaseService {
    public DefaultMicroService() {
        super();
    }

    @Override
    public final String setHost() {
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
