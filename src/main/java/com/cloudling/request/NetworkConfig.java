package com.cloudling.request;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.cloudling.request.listener.SupportCallback;
import com.cloudling.request.service.base.BaseService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * 描述: 网络请求配置类
 * 联系：1966353889@qq.com
 * 日期: 2021/12/3
 */
public class NetworkConfig implements SupportCallback {
    private static volatile NetworkConfig mInstance;
    private BaseService service;

    private NetworkConfig() {
    }

    public static NetworkConfig getInstance() {
        if (mInstance == null) {
            synchronized (NetworkConfig.class) {
                if (mInstance == null) {
                    mInstance = new NetworkConfig();
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取当前接口请求服务类
     */
    private BaseService getService() {
        return service;
    }

    /**
     * 设置当前接口请求服务类
     */
    @Override
    public void setService(BaseService service) {
        this.service = service;
    }

    @Override
    public void headers(ArrayMap<String, Object> headers) {
        if (getService() != null) {
            getService().headers(headers);
        }
    }

    @Override
    public void header(String key, Object value) {
        if (getService() != null) {
            getService().header(key, value);
        }
    }

    @Override
    public long getConnectTimeout() {
        return getService() != null ? getService().getConnectTimeout() : 0;
    }

    @Override
    public long getReadTimeout() {
        return getService() != null ? getService().getReadTimeout() : 0;
    }

    @Override
    public long getWriteTimeout() {
        return getService() != null ? getService().getWriteTimeout() : 0;
    }

    @Override
    public String getHost() {
        return getService() != null ? getService().getHost() : null;
    }

    @Override
    public String getHost(String name) {
        return getService() != null ? getService().getHost(name) : null;
    }

    @Override
    public ArrayMap<String, Object> getHeaders() {
        return getService() != null ? getService().getHeaders() : null;
    }

    /**
     * 用于页面异常关闭保存数据（在Activity对应的生命周期调用）
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            BaseService service = getService();
            if (service != null) {
                outState.putString("network_config_host", service.getHost());
                outState.putLong("network_config_connectTimeout", service.getConnectTimeout());
                outState.putLong("network_config_readTimeout", service.getReadTimeout());
                outState.putLong("network_config_writeTimeout", service.getWriteTimeout());
                try {
                    JSONObject microConfig = new JSONObject();
                    JSONObject headers = new JSONObject();
                    ArrayMap<String, String> microConfigMap = service.getMicroConfig();
                    ArrayMap<String, Object> headersMap = service.getHeaders();
                    if (microConfigMap != null) {
                        for (Map.Entry<String, String> entry : microConfigMap.entrySet()) {
                            microConfig.put(entry.getKey(), entry.getValue());
                        }
                        outState.putString("network_config_micro_config", microConfig.toString());
                    }
                    if (headersMap != null) {
                        for (Map.Entry<String, Object> entry : headersMap.entrySet()) {
                            headers.put(entry.getKey(), entry.getValue());
                        }
                        outState.putString("network_config_headers", headers.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 用于页面异常关闭恢复数据（在Activity对应的生命周期调用）
     */
    @Override
    public void onCreate(Bundle outState) {
        if (outState != null) {
            BaseService service = getService();
            if (service == null) {
                service = new BaseService() {
                    @Override
                    public String setHost() {
                        return outState.getString("network_config_host");
                    }

                    @Override
                    public ArrayMap<String, String> setMicroConfig() {
                        if (!TextUtils.isEmpty(outState.getString("network_config_micro_config"))) {
                            try {
                                ArrayMap<String, String> map = new ArrayMap<>();
                                JSONObject jsonObject = new JSONObject(outState.getString("network_config_micro_config"));
                                Iterator<String> iterator = jsonObject.keys();
                                while (iterator.hasNext()) {
                                    String key = iterator.next();
                                    map.put(key, jsonObject.getString(key));
                                }
                                return map;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }

                    @Override
                    public ArrayMap<String, Object> setHeaders() {
                        if (!TextUtils.isEmpty(outState.getString("network_config_headers"))) {
                            try {
                                ArrayMap<String, Object> map = new ArrayMap<>();
                                JSONObject jsonObject = new JSONObject(outState.getString("network_config_headers"));
                                Iterator<String> iterator = jsonObject.keys();
                                while (iterator.hasNext()) {
                                    String key = iterator.next();
                                    map.put(key, jsonObject.getString(key));
                                }
                                return map;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }

                    @Override
                    public long connectTimeout() {
                        return outState.getLong("network_config_connectTimeout");
                    }

                    @Override
                    public long readTimeout() {
                        return outState.getLong("network_config_readTimeout");
                    }

                    @Override
                    public long writeTimeout() {
                        return outState.getLong("network_config_writeTimeout");
                    }
                };
                setService(service);
            }
        }
    }
}
