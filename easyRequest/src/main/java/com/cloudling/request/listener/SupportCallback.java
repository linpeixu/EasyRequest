package com.cloudling.request.listener;

import android.os.Bundle;
import android.util.ArrayMap;

import com.cloudling.request.service.base.BaseService;

/**
 * 描述: 接口请求支持回调
 * 联系：1966353889@qq.com
 * 日期: 2021/12/3
 */
public interface SupportCallback {
    /**
     * 设置当前接口请求服务类
     */
    void setService(BaseService service);

    /**
     * 设置请求头
     */
    void headers(ArrayMap<String, Object> headers);

    /**
     * 设置请求头
     */
    void header(String key, Object value);

    /**
     * 获取连接超时时间
     */
    long getConnectTimeout();

    /**
     * 获取读取超时时间
     */
    long getReadTimeout();

    /**
     * 获取写入超时时间
     */
    long getWriteTimeout();

    /**
     * 获取host（单服务）
     */
    String getHost();

    /**
     * 获取host（微服务）
     *
     * @param name 微服务服务名
     */
    String getHost(String name);

    /**
     * 获取配置的请求头
     */
    ArrayMap<String, Object> getHeaders();

    /**
     * 用于页面异常关闭保存数据（在Activity对应的生命周期调用）
     */
    void onSaveInstanceState(Bundle outState);

    /**
     * 用于页面异常关闭恢复数据（在Activity对应的生命周期调用）
     */
    void onCreate(Bundle outState);
}
