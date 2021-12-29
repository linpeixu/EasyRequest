package com.cloudling.request.delegate;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.cloudling.request.network.BaseRequest;
import com.cloudling.request.network.EasyRequest;
import com.cloudling.request.network.NetworkConfig;
import com.cloudling.request.type.Method;
import com.cloudling.request.type.RequestType;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 描述: OkHttp请求代理
 * 联系：1966353889@qq.com
 * 日期: 2021/12/7
 */
public class OkHttpDelegate implements RequestDelegate {
    /**
     * okHttp请求实例
     */
    private OkHttpClient mOkHttpClient;
    /**
     * 缓存请求
     */
    private ConcurrentHashMap<String, BaseRequest<?, ?>> mRequestMap;

    public OkHttpDelegate() {
        mRequestMap = new ConcurrentHashMap<>();
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(NetworkConfig.getInstance().getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(NetworkConfig.getInstance().getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(NetworkConfig.getInstance().getWriteTimeout(), TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public @NotNull
                    Response intercept(@NotNull Chain chain) throws IOException {
                        Request.Builder mBuilder = chain.request().newBuilder().removeHeader("Accept-Encoding");
                        ArrayMap<String, Object> headersMap = NetworkConfig.getInstance().getHeaders();
                        if (headersMap != null) {
                            for (Map.Entry<String, Object> entry : headersMap.entrySet()) {
                                if (!TextUtils.isEmpty(entry.getKey()) && entry.getValue() != null) {
                                    mBuilder.header(entry.getKey(), (String) entry.getValue());
                                }
                            }
                        }
                        return chain.proceed(mBuilder.build());
                    }
                })
                .build();
    }

    public OkHttpDelegate(OkHttpClient okHttpClient) {
        mRequestMap = new ConcurrentHashMap<>();
        this.mOkHttpClient = okHttpClient;
    }

    @Override
    public <S, F> void request(BaseRequest<S, F> config) {
        if (config == null) {
            return;
        }
        mRequestMap.put(config.getUUid(), config);
        if (config.getListener() != null
                && mRequestMap.get(config.getUUid()) != null) {
            config.getListener().requestBefore();
        }
        Request.Builder builder = new Request.Builder();
        if (config.getMethod() == Method.GET || config.getType() == RequestType.GET) {
            if (config.getParams() != null) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(config.getUrl()).newBuilder();
                for (Map.Entry<String, Object> entry : config.getParams().entrySet()) {
                    urlBuilder.addQueryParameter(entry.getKey(), String.valueOf(entry.getValue()));
                }
                builder.url(urlBuilder.build());
            } else {
                builder.url(config.getUrl());
            }
        } else if (config.getType() == RequestType.POST
                || config.getType() == RequestType.PUT
                || config.getType() == RequestType.DELETE
                || config.getMethod() == Method.POST
                || config.getMethod() == Method.PUT
                || config.getMethod() == Method.DELETE) {
            builder.url(config.getUrl());
            RequestBody body;
            if (config.getParams() != null) {
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                String json = new JSONObject(config.getParams()).toString();
                body = RequestBody.create(mediaType, json);
            } else {
                body = RequestBody.create(null, "");
            }
            if (config.getMethod() == Method.POST || config.getType() == RequestType.POST) {
                builder.post(body);
            } else if (config.getMethod() == Method.PUT || config.getType() == RequestType.PUT) {
                builder.put(body);
            } else if (config.getMethod() == Method.DELETE || config.getType() == RequestType.DELETE) {
                builder.delete(body);
            }
        } else {
            if (mRequestMap.get(config.getUUid()) != null) {
                if (config.getListener() != null) {
                    config.getListener().onFail("未知的请求方法-" + config.getType().name());
                    config.getListener().requestAfter();
                }
                removeByUUID(config.getUUid());
            }
            return;
        }
        Request request = builder.build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                String result = e.toString();
                if (mRequestMap.get(config.getUUid()) != null) {
                    if (config.getListener() != null) {
                        config.getListener().onFail(e.toString());
                        config.getListener().requestAfter();
                    }
                    removeByUUID(config.getUUid());
                } else {
                    EasyRequest.getInstance().logE("[onFailure-no callback]result：" + result);
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body() != null ? response.body().string() : null;
                if (mRequestMap.get(config.getUUid()) != null) {
                    if (config.getListener() != null) {
                        if (response.isSuccessful()) {
                            config.getListener().onSuccess(result);
                        } else {
                            config.getListener().onFail(result);
                        }
                        config.getListener().requestAfter();
                    }
                    removeByUUID(config.getUUid());
                } else {
                    EasyRequest.getInstance().logI("[onResponse-no callback]result：" + result);
                }
            }
        });

    }

    @Override
    public <S, F> void remove(BaseRequest<S, F> config) {
        if (config != null) {
            mRequestMap.remove(config.getUUid());
        }
    }

    @Override
    public void removeByTag(String TAG) {
        if (!TextUtils.isEmpty(TAG)) {
            for (String key : mRequestMap.keySet()) {
                if (mRequestMap.get(key) != null
                        && !TextUtils.isEmpty(mRequestMap.get(key).getTAG())
                        && mRequestMap.get(key).getTAG().equals(TAG)) {
                    mRequestMap.remove(key);
                }
            }
        }
    }

    @Override
    public void removeByUUID(String uuid) {
        mRequestMap.remove(uuid);
    }

}
