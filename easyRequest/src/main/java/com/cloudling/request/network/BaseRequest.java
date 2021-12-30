package com.cloudling.request.network;

import android.text.TextUtils;

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
    Map<String, Object> params;
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
                    if (EasyRequest.getInstance().getHandler() != null) {
                        EasyRequest.getInstance().getHandler().post(new Runnable() {
                            @Override
                            public void run() {
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
                                        EasyRequest.getInstance().logI("[onSuccess]result：" + realResult);
                                    } else if (transformListener.callbackType() == TransformCallbackType.FAIL) {
                                        if (requestListener != null) {
                                            requestListener.onFail(realResult);
                                        }
                                        if (converterListener != null && converterFactory != null) {
                                            converterListener.onFail(converterFactory.converterFail(realResult));
                                        }
                                        EasyRequest.getInstance().logE("[onFail]result：" + realResult);
                                    }
                                } else {
                                    if (requestListener != null) {
                                        requestListener.onSuccess(result);
                                    }
                                    if (converterListener != null && converterFactory != null) {
                                        converterListener.onSuccess(converterFactory.converterSuccess(result));
                                    }
                                    EasyRequest.getInstance().logI("[onSuccess]result：" + result);
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
                                EasyRequest.getInstance().logI("[onSuccess]result：" + realResult);
                            } else if (transformListener.callbackType() == TransformCallbackType.FAIL) {
                                if (requestListener != null) {
                                    requestListener.onFail(realResult);
                                }
                                if (converterListener != null && converterFactory != null) {
                                    converterListener.onFail(converterFactory.converterFail(realResult));
                                }
                                EasyRequest.getInstance().logE("[onFail]result：" + realResult);
                            }
                        } else {
                            if (requestListener != null) {
                                requestListener.onSuccess(result);
                            }
                            if (converterListener != null && converterFactory != null) {
                                converterListener.onSuccess(converterFactory.converterSuccess(result));
                            }
                            EasyRequest.getInstance().logI("[onSuccess]result：" + result);
                        }
                    }
                }

                @Override
                public void onFail(String result) {
                    if (EasyRequest.getInstance().getHandler() != null) {
                        EasyRequest.getInstance().getHandler().post(new Runnable() {
                            @Override
                            public void run() {
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
                                        EasyRequest.getInstance().logE("[onFail]result：" + realResult);
                                    } else if (transformListener.callbackType() == TransformCallbackType.SUCCESS) {
                                        if (requestListener != null) {
                                            requestListener.onSuccess(realResult);
                                        }
                                        if (converterListener != null && converterFactory != null) {
                                            converterListener.onSuccess(converterFactory.converterSuccess(realResult));
                                        }
                                        EasyRequest.getInstance().logI("[onSuccess]result：" + realResult);
                                    }
                                } else {
                                    if (requestListener != null) {
                                        requestListener.onFail(result);
                                    }
                                    if (converterListener != null && converterFactory != null) {
                                        converterListener.onFail(converterFactory.converterFail(result));
                                    }
                                    EasyRequest.getInstance().logE("[onFail]result：" + result);
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
                                EasyRequest.getInstance().logE("[onFail]result：" + realResult);
                            } else if (transformListener.callbackType() == TransformCallbackType.SUCCESS) {
                                if (requestListener != null) {
                                    requestListener.onSuccess(realResult);
                                }
                                if (converterListener != null && converterFactory != null) {
                                    converterListener.onSuccess(converterFactory.converterSuccess(realResult));
                                }
                                EasyRequest.getInstance().logI("[onSuccess]result：" + realResult);
                            }
                        } else {
                            if (requestListener != null) {
                                requestListener.onFail(result);
                            }
                            if (converterListener != null && converterFactory != null) {
                                converterListener.onFail(converterFactory.converterFail(result));
                            }
                            EasyRequest.getInstance().logE("[onFail]result：" + result);
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

    public Map<String, Object> getParams() {
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

    public static class Builder<S, F> {
        RequestType requestType;
        Method method;
        Map<String, Object> params;
        RequestListener requestListener;
        TransformListener transformListener;
        MockRequest mockRequest;
        String host;
        String path;
        String name;
        String TAG;
        BaseConverterFactory<S, F> converterFactory;
        ConverterListener<S, F> converterListener;

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

        public Builder<S, F> params(Map<String, Object> params) {
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

        public BaseRequest<S, F> build() {
            return new BaseRequest<>(this);
        }
    }


}
