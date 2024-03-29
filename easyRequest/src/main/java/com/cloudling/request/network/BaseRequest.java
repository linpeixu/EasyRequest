package com.cloudling.request.network;

import android.net.Uri;
import android.text.TextUtils;

import com.cloudling.request.cache.AesHelper;
import com.cloudling.request.cache.CacheHelper;
import com.cloudling.request.cache.CacheType;
import com.cloudling.request.cache.ReadCacheType;
import com.cloudling.request.cache.TimeUnit;
import com.cloudling.request.converter.BaseConverterFactory;
import com.cloudling.request.listener.ConverterListener;
import com.cloudling.request.listener.MockRequest;
import com.cloudling.request.listener.RequestListener;
import com.cloudling.request.listener.TransformListener;
import com.cloudling.request.listener.WholeConverterListener;
import com.cloudling.request.listener.WholeRequestListener;
import com.cloudling.request.type.Method;
import com.cloudling.request.type.OriginalCallback;
import com.cloudling.request.type.RequestType;
import com.cloudling.request.type.TransformCallbackType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 描述: 请求配置
 * 联系：1966353889@qq.com
 * 日期: 2021/12/29
 */
public class BaseRequest<S, F> {
    /**
     * 请求类型
     */
    RequestType requestType;
    /**
     * 请求类型
     */
    Method method;
    /**
     * 请求参数
     */
    LinkedHashMap<String, Object> params;
    /**
     * 请求回调（外部设置）
     */
    RequestListener requestListener;
    /**
     * 真正的请求回调
     */
    WholeRequestListener realRequestListener;
    /**
     * 转换请求结果（可对请求回调参数做包装）
     */
    TransformListener transformListener;
    /**
     * 模拟请求配置
     */
    MockRequest mockRequest;
    /**
     * 单服务host
     */
    String host;
    /**
     * 请求path
     */
    String path;
    /**
     * 微服务名（需在BaseService配置）
     */
    String name;
    /**
     * 唯一值
     */
    final String uuid;
    final String TAG;
    final BaseConverterFactory<S, F> converterFactory;
    final ConverterListener<S, F> converterListener;
    /**
     * 缓存的类型
     */
    CacheType cacheType;
    /**
     * 缓存时长
     */
    long saveDuration;
    /**
     * 缓存时长的单位
     */
    TimeUnit cacheTimeUnit;
    /**
     * 读取缓存的类型
     */
    ReadCacheType readCacheType;
    /**
     * 缓存的key（未加密）
     */
    String cacheKey;

    private BaseRequest(Builder<S, F> builder) {
        uuid = UUID.randomUUID().toString();
        requestType = builder.requestType;
        method = builder.method;
        params = builder.params;
        requestListener = builder.requestListener;
        transformListener = builder.transformListener;
        mockRequest = builder.mockRequest;
        host = builder.host;
        path = builder.path;
        name = builder.name;
        TAG = builder.TAG;
        converterFactory = builder.converterFactory;
        converterListener = builder.converterListener;
        if (builder.cacheType == null) {
            cacheType = CacheType.NO;
        } else {
            cacheType = builder.cacheType;
        }
        saveDuration = builder.saveDuration;
        if (saveDuration != -1 && builder.cacheTimeUnit == null) {
            cacheTimeUnit = TimeUnit.SECONDS;
        } else {
            cacheTimeUnit = builder.cacheTimeUnit;
        }
        if (builder.readCacheType == null) {
            readCacheType = ReadCacheType.NO;
        } else {
            readCacheType = builder.readCacheType;
        }
        cacheKey = builder.cacheKey;
        if (requestListener != null || converterListener != null) {
            realRequestListener = new WholeRequestListener() {
                @Override
                public void requestBefore() {
                    if (EasyRequest.getInstance().getHandler() != null) {
                        EasyRequest.getInstance().getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (requestListener instanceof WholeRequestListener) {
                                    ((WholeRequestListener) requestListener).requestBefore();
                                }
                                if (converterListener instanceof WholeConverterListener) {
                                    ((WholeConverterListener<S, F>) converterListener).requestBefore();
                                }
                            }
                        });
                    } else {
                        if (requestListener instanceof WholeRequestListener) {
                            ((WholeRequestListener) requestListener).requestBefore();
                        }
                        if (converterListener instanceof WholeConverterListener) {
                            ((WholeConverterListener<S, F>) converterListener).requestBefore();
                        }
                    }
                }

                @Override
                public void requestAfter() {
                    if (EasyRequest.getInstance().getHandler() != null) {
                        EasyRequest.getInstance().getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (requestListener instanceof WholeRequestListener) {
                                    ((WholeRequestListener) requestListener).requestAfter();
                                }
                                if (converterListener instanceof WholeConverterListener) {
                                    ((WholeConverterListener<S, F>) converterListener).requestAfter();
                                }
                            }
                        });
                    } else {
                        if (requestListener instanceof WholeRequestListener) {
                            ((WholeRequestListener) requestListener).requestAfter();
                        }
                        if (converterListener instanceof WholeConverterListener) {
                            ((WholeConverterListener<S, F>) converterListener).requestAfter();
                        }
                    }
                }

                @Override
                public void onSuccess(String result) {
                    if (readCacheType == ReadCacheType.DEFAULT || readCacheType == ReadCacheType.READ_SUCCESS_AFTER_SUCCESS || readCacheType == ReadCacheType.READ_FAIL_AFTER_SUCCESS) {
                        String readKey = AesHelper.encryptAsString(getCacheKey() + (readCacheType == ReadCacheType.READ_FAIL_AFTER_SUCCESS ? OriginalCallback.FAILURE.name() : OriginalCallback.SUCCESS.name()));
                        String cache = CacheHelper.getInstance().getCache(readKey);
                        if (cacheType == CacheType.SOURCE_SUCCESS || cacheType == CacheType.DEFAULT) {
                            String writeKey = AesHelper.encryptAsString(getCacheKey() + OriginalCallback.SUCCESS.name());
                            EasyRequest.getInstance().logD("[BaseRequest-onSuccess]\n执行缓存->原始key:" + getCacheKey() + OriginalCallback.SUCCESS.name() + "\n执行缓存->加密key:" + writeKey);
                            if (saveDuration == -1) {
                                CacheHelper.getInstance().addCache(writeKey, result);
                            } else {
                                CacheHelper.getInstance().addCache(writeKey, result, saveDuration, cacheTimeUnit);
                            }
                        }
                        EasyRequest.getInstance().logD("[BaseRequest-onSuccess]\n读取缓存->readKey:" + readKey + "\n读取缓存->cache:" + cache);
                        if (!TextUtils.isEmpty(cache)) {
                            result = cache;
                            if (readCacheType == ReadCacheType.READ_FAIL_AFTER_SUCCESS) {
                                /*当前网络请求成功，但是设置了读取失败的缓存数据，所以需要将请求强制转换为失败*/
                                /*这里需要注意的是如果设置了transformListener，这里强制转换结果为失败*/
                                if (transformListener == null || transformListener.callbackType() != TransformCallbackType.FAIL) {
                                    EasyRequest.getInstance().logD("BaseRequest-[onSuccess]\n读取缓存成功->强制将结果转为fail");
                                    TransformListener cacheTransformListener = transformListener;
                                    transformListener = new TransformListener() {
                                        @Override
                                        public String onTransformResult(String result) {
                                            return cacheTransformListener == null ? result : cacheTransformListener instanceof OriginalTransformListener ? (((OriginalTransformListener) cacheTransformListener).onTransformResult(OriginalCallback.FAILURE, result)) : cacheTransformListener.onTransformResult(result);
                                        }

                                        @Override
                                        public TransformCallbackType callbackType() {
                                            return TransformCallbackType.FAIL;
                                        }
                                    };
                                }
                            }
                        }
                    } else {
                        if (cacheType == CacheType.SOURCE_SUCCESS || cacheType == CacheType.DEFAULT) {
                            String writeKey = AesHelper.encryptAsString(getCacheKey() + OriginalCallback.SUCCESS.name());
                            EasyRequest.getInstance().logD("[BaseRequest-onSuccess]\n执行缓存->原始key:" + getCacheKey() + OriginalCallback.SUCCESS.name() + "\n执行缓存->加密key:" + writeKey);
                            if (saveDuration == -1) {
                                CacheHelper.getInstance().addCache(writeKey, result);
                            } else {
                                CacheHelper.getInstance().addCache(writeKey, result, saveDuration, cacheTimeUnit);
                            }
                        }
                    }
                    if (EasyRequest.getInstance().getHandler() != null) {
                        String finalResult = result;
                        EasyRequest.getInstance().getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (transformListener != null) {
                                    String realResult = transformListener instanceof OriginalTransformListener ? ((OriginalTransformListener) transformListener).onTransformResult(OriginalCallback.SUCCESS, finalResult) : transformListener.onTransformResult(finalResult);
                                    if (transformListener.callbackType() == TransformCallbackType.DEFAULT
                                            || transformListener.callbackType() == TransformCallbackType.SUCCESS) {
                                        if (requestListener != null) {
                                            requestListener.onSuccess(realResult);
                                        }
                                        if (converterListener != null && converterFactory != null) {
                                            converterListener.onSuccess(converterFactory.converterSuccess(realResult));
                                        }
                                        EasyRequest.getInstance().logI("[onSuccess]\nresult:" + realResult);
                                    } else if (transformListener.callbackType() == TransformCallbackType.FAIL) {
                                        if (requestListener != null) {
                                            requestListener.onFail(realResult);
                                        }
                                        if (converterListener != null && converterFactory != null) {
                                            converterListener.onFail(converterFactory.converterFail(realResult));
                                        }
                                        EasyRequest.getInstance().logE("[onFail]\nresult:" + realResult);
                                    }
                                } else {
                                    if (requestListener != null) {
                                        requestListener.onSuccess(finalResult);
                                    }
                                    if (converterListener != null && converterFactory != null) {
                                        converterListener.onSuccess(converterFactory.converterSuccess(finalResult));
                                    }
                                    EasyRequest.getInstance().logI("[onSuccess]\nresult:" + finalResult);
                                }
                            }
                        });
                    } else {
                        if (transformListener != null) {
                            String realResult = transformListener instanceof OriginalTransformListener ? ((OriginalTransformListener) transformListener).onTransformResult(OriginalCallback.SUCCESS, result) : transformListener.onTransformResult(result);
                            if (transformListener.callbackType() == TransformCallbackType.DEFAULT
                                    || transformListener.callbackType() == TransformCallbackType.SUCCESS) {
                                if (requestListener != null) {
                                    requestListener.onSuccess(realResult);
                                }
                                if (converterListener != null && converterFactory != null) {
                                    converterListener.onSuccess(converterFactory.converterSuccess(realResult));
                                }
                                EasyRequest.getInstance().logI("[BaseRequest-onSuccess]\nresult:" + realResult);
                            } else if (transformListener.callbackType() == TransformCallbackType.FAIL) {
                                if (requestListener != null) {
                                    requestListener.onFail(realResult);
                                }
                                if (converterListener != null && converterFactory != null) {
                                    converterListener.onFail(converterFactory.converterFail(realResult));
                                }
                                EasyRequest.getInstance().logE("[BaseRequest-onFail]\nresult:" + realResult);
                            }
                        } else {
                            if (requestListener != null) {
                                requestListener.onSuccess(result);
                            }
                            if (converterListener != null && converterFactory != null) {
                                converterListener.onSuccess(converterFactory.converterSuccess(result));
                            }
                            EasyRequest.getInstance().logI("[BaseRequest-onSuccess]\nresult:" + result);
                        }
                    }
                }

                @Override
                public void onFail(String result) {
                    if (readCacheType == ReadCacheType.DEFAULT || readCacheType == ReadCacheType.READ_SUCCESS_AFTER_FAIL || readCacheType == ReadCacheType.READ_FAIL_AFTER_FAIL) {
                        String readKey = AesHelper.encryptAsString(getCacheKey() + (readCacheType == ReadCacheType.READ_SUCCESS_AFTER_FAIL ? OriginalCallback.SUCCESS.name() : OriginalCallback.FAILURE.name()));
                        String cache = CacheHelper.getInstance().getCache(readKey);
                        if (cacheType == CacheType.SOURCE_FAIL || cacheType == CacheType.DEFAULT) {
                            String writeKey = AesHelper.encryptAsString(getCacheKey() + OriginalCallback.FAILURE.name());
                            EasyRequest.getInstance().logD("[BaseRequest-onFail]\n执行缓存->原始key:" + getCacheKey() + OriginalCallback.FAILURE.name() + "\n执行缓存->加密key:" + writeKey);
                            if (saveDuration == -1) {
                                CacheHelper.getInstance().addCache(writeKey, result);
                            } else {
                                CacheHelper.getInstance().addCache(writeKey, result, saveDuration, cacheTimeUnit);
                            }
                        }
                        EasyRequest.getInstance().logD("[BaseRequest-onFail]\n读取缓存->readKey:" + readKey + "\n读取缓存->cache:" + cache);
                        if (!TextUtils.isEmpty(cache)) {
                            result = cache;
                            if (readCacheType == ReadCacheType.READ_SUCCESS_AFTER_FAIL) {
                                /*当前网络请求失败，但是设置了读取成功的缓存数据，所以需要将请求强制转换为成功*/
                                /*这里需要注意的是如果设置了transformListener，这里强制转换结果为成功*/
                                if (transformListener == null || transformListener.callbackType() != TransformCallbackType.SUCCESS) {
                                    EasyRequest.getInstance().logD("[BaseRequest-onFail]\n读取缓存成功->强制将结果转为success");
                                    TransformListener cacheTransformListener = transformListener;
                                    transformListener = new TransformListener() {
                                        @Override
                                        public String onTransformResult(String result) {
                                            return cacheTransformListener == null ? result : cacheTransformListener instanceof OriginalTransformListener ? (((OriginalTransformListener) cacheTransformListener).onTransformResult(OriginalCallback.SUCCESS, result)) : cacheTransformListener.onTransformResult(result);
                                        }

                                        @Override
                                        public TransformCallbackType callbackType() {
                                            return TransformCallbackType.SUCCESS;
                                        }
                                    };
                                }
                            }
                        }
                    } else {
                        if (cacheType == CacheType.SOURCE_FAIL || cacheType == CacheType.DEFAULT) {
                            String writeKey = AesHelper.encryptAsString(getCacheKey() + OriginalCallback.FAILURE.name());
                            EasyRequest.getInstance().logD("[BaseRequest-onFail]\n执行缓存->原始key:" + getCacheKey() + OriginalCallback.FAILURE.name() + "\n执行缓存->加密key:" + writeKey);
                            if (saveDuration == -1) {
                                CacheHelper.getInstance().addCache(writeKey, result);
                            } else {
                                CacheHelper.getInstance().addCache(writeKey, result, saveDuration, cacheTimeUnit);
                            }
                        }
                    }
                    if (EasyRequest.getInstance().getHandler() != null) {
                        String finalResult = result;
                        EasyRequest.getInstance().getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (transformListener != null) {
                                    String realResult = transformListener instanceof OriginalTransformListener ? ((OriginalTransformListener) transformListener).onTransformResult(OriginalCallback.FAILURE, finalResult) : transformListener.onTransformResult(finalResult);
                                    if (transformListener.callbackType() == TransformCallbackType.DEFAULT
                                            || transformListener.callbackType() == TransformCallbackType.FAIL) {
                                        if (requestListener != null) {
                                            requestListener.onFail(realResult);
                                        }
                                        if (converterListener != null && converterFactory != null) {
                                            converterListener.onFail(converterFactory.converterFail(realResult));
                                        }
                                        EasyRequest.getInstance().logE("[BaseRequest-onFail]\nresult:" + realResult);
                                    } else if (transformListener.callbackType() == TransformCallbackType.SUCCESS) {
                                        if (requestListener != null) {
                                            requestListener.onSuccess(realResult);
                                        }
                                        if (converterListener != null && converterFactory != null) {
                                            converterListener.onSuccess(converterFactory.converterSuccess(realResult));
                                        }
                                        EasyRequest.getInstance().logI("[BaseRequest-onSuccess]\nresult:" + realResult);
                                    }
                                } else {
                                    if (requestListener != null) {
                                        requestListener.onFail(finalResult);
                                    }
                                    if (converterListener != null && converterFactory != null) {
                                        converterListener.onFail(converterFactory.converterFail(finalResult));
                                    }
                                    EasyRequest.getInstance().logE("[BaseRequest-onFail]\nresult:" + finalResult);
                                }
                            }
                        });
                    } else {
                        if (transformListener != null) {
                            String realResult = transformListener instanceof OriginalTransformListener ? ((OriginalTransformListener) transformListener).onTransformResult(OriginalCallback.FAILURE, result) : transformListener.onTransformResult(result);
                            if (transformListener.callbackType() == TransformCallbackType.DEFAULT
                                    || transformListener.callbackType() == TransformCallbackType.FAIL) {
                                if (requestListener != null) {
                                    requestListener.onFail(realResult);
                                }
                                if (converterListener != null && converterFactory != null) {
                                    converterListener.onFail(converterFactory.converterFail(realResult));
                                }
                                EasyRequest.getInstance().logE("[BaseRequest-onFail]\nresult:" + realResult);
                            } else if (transformListener.callbackType() == TransformCallbackType.SUCCESS) {
                                if (requestListener != null) {
                                    requestListener.onSuccess(realResult);
                                }
                                if (converterListener != null && converterFactory != null) {
                                    converterListener.onSuccess(converterFactory.converterSuccess(realResult));
                                }
                                EasyRequest.getInstance().logI("[BaseRequest-onSuccess]\nresult:" + realResult);
                            }
                        } else {
                            if (requestListener != null) {
                                requestListener.onFail(result);
                            }
                            if (converterListener != null && converterFactory != null) {
                                converterListener.onFail(converterFactory.converterFail(result));
                            }
                            EasyRequest.getInstance().logE("[BaseRequest-onFail]\nresult:" + result);
                        }
                    }
                }
            };
        }
    }

    public String getUUid() {
        return uuid;
    }

    public String getTAG() {
        return TAG;
    }

    /**
     * @deprecated 推荐使用 {@link #getMethod()}
     */
    public RequestType getType() {
        return requestType;
    }

    public Method getMethod() {
        return method;
    }

    public LinkedHashMap<String, Object> getParams() {
        return params;
    }

    public WholeRequestListener getListener() {
        return realRequestListener;
    }

    public String getUrl() {
        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(name)) {
            builder.append(NetworkConfig.getInstance().getHost(name));
        } else {
            builder.append(!TextUtils.isEmpty(host) ? host : NetworkConfig.getInstance().getHost());
        }
        if (!TextUtils.isEmpty(path)) {
            builder.append(path);
        }
        return builder.toString();
    }

    /**
     * 获取用于缓存的key（未加密）
     */
    public String getCacheKey() {
        if (!TextUtils.isEmpty(cacheKey)) {
            return cacheKey;
        }
        StringBuilder builder = new StringBuilder();
        if (method != null) {
            builder.append(method.name());
        } else if (requestType != null) {
            builder.append(requestType.name());
        }
        if (!TextUtils.isEmpty(name)) {
            builder.append(NetworkConfig.getInstance().getHost(name));
        } else {
            builder.append(!TextUtils.isEmpty(host) ? host : NetworkConfig.getInstance().getHost());
        }
        if (!TextUtils.isEmpty(path)) {
            builder.append(path);
        }
        if (method == Method.GET || requestType == RequestType.GET) {
            if (params != null) {
                Uri.Builder uriBuilder = Uri.parse(builder.toString()).buildUpon();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    uriBuilder.appendQueryParameter(entry.getKey(), String.valueOf(entry.getValue()));
                }
                return uriBuilder.build().toString();
            }
        }
        return builder.toString();
    }

    public static class Builder<S, F> {
        RequestType requestType;
        Method method;
        LinkedHashMap<String, Object> params;
        RequestListener requestListener;
        TransformListener transformListener;
        MockRequest mockRequest;
        String host;
        String path;
        String name;
        String TAG;
        BaseConverterFactory<S, F> converterFactory;
        ConverterListener<S, F> converterListener;
        CacheType cacheType;
        long saveDuration;
        TimeUnit cacheTimeUnit;
        ReadCacheType readCacheType;
        String cacheKey;

        /**
         * @deprecated 推荐使用 {@link Builder#method(Method)}
         */
        public Builder<S, F> type(RequestType requestType) {
            this.requestType = requestType;
            return this;
        }

        public Builder<S, F> method(Method method) {
            this.method = method;
            return this;
        }

        public Builder<S, F> params(LinkedHashMap<String, Object> params) {
            this.params = params;
            return this;
        }

        public Builder<S, F> listener(RequestListener listener) {
            this.requestListener = listener;
            return this;
        }

        public Builder<S, F> listener(ConverterListener<S, F> converterListener) {
            this.converterListener = converterListener;
            return this;
        }

        public Builder<S, F> transformResult(TransformListener listener) {
            this.transformListener = listener;
            return this;
        }

        public Builder<S, F> mockRequest(MockRequest mockRequest) {
            this.mockRequest = mockRequest;
            return this;
        }

        public Builder<S, F> host(String host) {
            this.host = host;
            return this;
        }

        public Builder<S, F> path(String path) {
            this.path = path;
            return this;
        }

        public Builder<S, F> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<S, F> tag(String TAG) {
            this.TAG = TAG;
            return this;
        }

        public Builder<S, F> addConverterFactory(BaseConverterFactory<S, F> converterFactory) {
            this.converterFactory = converterFactory;
            return this;
        }

        public Builder<S, F> cache(CacheType cacheType) {
            this.cacheType = cacheType;
            this.saveDuration = -1;
            return this;
        }

        public Builder<S, F> cache(CacheType cacheType, long saveDuration) {
            this.cacheType = cacheType;
            this.saveDuration = saveDuration;
            return this;
        }

        public Builder<S, F> cache(CacheType cacheType, long saveDuration, TimeUnit timeUnit) {
            this.cacheType = cacheType;
            this.saveDuration = saveDuration;
            this.cacheTimeUnit = timeUnit;
            return this;
        }

        public Builder<S, F> readCache(ReadCacheType readCacheType) {
            this.readCacheType = readCacheType;
            return this;
        }

        public Builder<S, F> cacheKey(String cacheKey) {
            this.cacheKey = cacheKey;
            return this;
        }

        public BaseRequest<S, F> build() {
            return new BaseRequest<>(this);
        }
    }


}
