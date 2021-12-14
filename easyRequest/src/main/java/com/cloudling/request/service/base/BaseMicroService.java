package com.cloudling.request.service.base;

/**
 * 描述: 微服务接口请求服务基类（不同环境继承该类设置不同的host）
 * 联系：1966353889@qq.com
 * 日期: 2021/12/3
 */
public abstract class BaseMicroService extends BaseService {
    public BaseMicroService() {
        super();
    }

    @Override
    public final String setHost() {
        return null;
    }
}
