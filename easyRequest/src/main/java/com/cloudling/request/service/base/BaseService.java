package com.cloudling.request.service.base;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.cloudling.request.listener.TimeoutListener;

/**
 * 描述: 接口请求服务基类
 * 联系：1966353889@qq.com
 * 日期: 2021/12/3
 */
public abstract class BaseService implements TimeoutListener {
    /**
     * 连接超时时间
     */
    private final long mConnectTimeout;
    /**
     * 读取超时时间
     */
    private final long mReadTimeout;
    /**
     * 写入超时时间
     */
    private final long mWriteTimeout;
    /**
     * 单服务接口host
     */
    private final String mHost;
    /**
     * 微服务键值对
     */
    private final ArrayMap<String, String> mMicroConfig;
    /**
     * 请求头
     */
    private ArrayMap<String, Object> mHeaders;

    public BaseService() {
        /*设置连接超时时间*/
        mConnectTimeout = connectTimeout();
        /*设置读取超时时间*/
        mReadTimeout = readTimeout();
        /*设置写入超时时间*/
        mWriteTimeout = writeTimeout();
        /*设置单服务接口host*/
        mHost = setHost();
        /*设置微服务键值对*/
        mMicroConfig = setMicroConfig();
        /*设置请求头*/
        mHeaders = setHeaders();
    }

    /**
     * 设置单服务接口host
     */
    public abstract String setHost();

    /**
     * 设置微服务键值对（服务名与对应的Host）
     */
    public abstract ArrayMap<String, String> setMicroConfig();

    /**
     * 设置请求头
     */
    public abstract ArrayMap<String, Object> setHeaders();

    /**
     * 获取连接超时时间
     */
    public long getConnectTimeout() {
        return mConnectTimeout;
    }

    /**
     * 获取读取超时时间
     */
    public long getReadTimeout() {
        return mReadTimeout;
    }

    /**
     * 获取写入超时时间
     */
    public long getWriteTimeout() {
        return mWriteTimeout;
    }

    /**
     * 获取host（单服务）
     */
    public String getHost() {
        return mHost;
    }

    /**
     * 获取host（微服务）
     *
     * @param name 微服务服务名
     */
    public String getHost(String name) {
        return mMicroConfig != null && !TextUtils.isEmpty(name) ? mMicroConfig.get(name) : null;
    }

    /**
     * 获取微服务配置
     */
    public ArrayMap<String, String> getMicroConfig() {
        return mMicroConfig;
    }

    /**
     * 获取请求头
     */
    public ArrayMap<String, Object> getHeaders() {
        return mHeaders;
    }

    /**
     * 设置请求头
     */
    public void headers(ArrayMap<String, Object> headers) {
        mHeaders = headers;
    }

    /**
     * 设置请求头
     */
    public void header(String key, Object value) {
        if (mHeaders == null) {
            mHeaders = new ArrayMap<>();
        }
        mHeaders.put(key, value);
    }
}
