package com.cloudling.request;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.cloudling.request.converter.BaseConverterFactory;
import com.cloudling.request.delegate.OkHttpDelegate;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 描述: 网络请求发起类（默认通过okhttp实现）
 * 联系：1966353889@qq.com
 * 日期: 2021/12/7
 */
public class EasyRequest {
    private final String TAG = "EasyRequest";
    private static volatile EasyRequest mInstance;
    /**
     * 请求代理
     */
    private RequestDelegate mRequestDelegate;
    /**
     * 日志输出类
     */
    private ILog mILog;
    /**
     * 是否调试模式
     */
    private boolean debug = BuildConfig.DEBUG;
    private Handler mHandler;

    private EasyRequest() {
        /*默认设置请求代理为okHttp*/
        mRequestDelegate = new OkHttpDelegate();
        mHandler = new Handler(Looper.getMainLooper());
        mILog = new ILog() {
            @Override
            public void onLogVerbose(String TAG, String log) {
                if (debug) {
                    Log.v(TAG, log);
                }
            }

            @Override
            public void onLogDebug(String TAG, String log) {
                if (debug) {
                    Log.d(TAG, log);
                }
            }

            @Override
            public void onLogInfo(String TAG, String log) {
                if (debug) {
                    Log.i(TAG, log);
                }
            }

            @Override
            public void onLogWarn(String TAG, String log) {
                if (debug) {
                    Log.w(TAG, log);
                }
            }

            @Override
            public void onLogError(String TAG, String log) {
                if (debug) {
                    Log.e(TAG, log);
                }
            }
        };
    }

    private Handler getHandler() {
        return mHandler;
    }

    /**
     * 延时处理任务
     */
    private void delayTask(Handler handler, long delayMillis, Runnable runnable) {
        if (delayMillis > 0) {
            handler.postDelayed(runnable, delayMillis);
        } else {
            handler.post(runnable);
        }
    }

    public static EasyRequest getInstance() {
        if (mInstance == null) {
            synchronized (EasyRequest.class) {
                if (mInstance == null) {
                    mInstance = new EasyRequest();
                }
            }
        }
        return mInstance;
    }

    /**
     * 设置是否打开调试模式
     */
    public void enableDebug(boolean debug) {
        this.debug = debug;
        if (canPrintLog()) {
            logD("[enableDebug]");
        }
    }

    /**
     * 设置对应的log处理类
     */
    public void setILog(ILog log) {
        if (log != null) {
            mILog = log;
            if (canPrintLog()) {
                logD("[setILog]");
            }
        }
    }

    /**
     * 设置请求代理
     */
    public void setRequestDelegate(RequestDelegate delegate) {
        mRequestDelegate = delegate;
        if (canPrintLog() && delegate != null) {
            logD("[setRequestDelegate]" + delegate.getClass().getName());
        }
    }

    /**
     * 打印log
     */
    public void logV(String log) {
        if (canPrintLog()) {
            mILog.onLogVerbose(TAG, log);
        }
    }

    /**
     * 打印log
     */
    public void logV(String TAG, String log) {
        if (canPrintLog()) {
            mILog.onLogVerbose(TAG, log);
        }
    }

    /**
     * 打印log
     */
    public void logD(String log) {
        if (canPrintLog()) {
            mILog.onLogDebug(TAG, log);
        }
    }

    /**
     * 打印log
     */
    public void logD(String TAG, String log) {
        if (canPrintLog()) {
            mILog.onLogDebug(TAG, log);
        }
    }

    /**
     * 打印log
     */
    public void logI(String log) {
        if (canPrintLog()) {
            mILog.onLogInfo(TAG, log);
        }
    }

    /**
     * 打印log
     */
    public void logI(String TAG, String log) {
        if (canPrintLog()) {
            mILog.onLogInfo(TAG, log);
        }
    }

    /**
     * 打印log
     */
    public void logW(String log) {
        if (canPrintLog()) {
            mILog.onLogWarn(TAG, log);
        }
    }

    /**
     * 打印log
     */
    public void logW(String TAG, String log) {
        if (canPrintLog()) {
            mILog.onLogWarn(TAG, log);
        }
    }

    /**
     * 打印log
     */
    public void logE(String log) {
        if (canPrintLog()) {
            mILog.onLogError(TAG, log);
        }
    }

    /**
     * 打印log
     */
    public void logE(String TAG, String log) {
        if (canPrintLog()) {
            mILog.onLogError(TAG, log);
        }
    }


    /**
     * 是否可输出调试日志
     */
    public boolean canPrintLog() {
        return debug && mILog != null;
    }

    public <S, F> void request(Request<S, F> config) {
        if (canPrintLog() && config != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("[request]")
                    .append("url：")
                    .append(config.getUrl())
                    .append("，type：")
                    .append(config.requestType.name())
                    .append("，params：")
                    .append(config.params != null ? new JSONObject(config.params).toString() : null);
            logD(builder.toString());
        }
        if (config != null && config.mockRequest != null && config.getListener() != null) {
            Handler dealWithHandler = Looper.myLooper() == Looper.getMainLooper() || config.getSchedulers() == Schedulers.MAIN ? mHandler : new Handler();
            delayTask(dealWithHandler, 0, new Runnable() {
                @Override
                public void run() {
                    config.getListener().requestBefore();
                }
            });
            delayTask(dealWithHandler, config.mockRequest.requestDuration() * 1000L, new Runnable() {
                @Override
                public void run() {
                    if (config.mockRequest.callbackType() == MockRequestCallbackType.SUCCESS) {
                        config.getListener().onSuccess(config.mockRequest.result());
                    } else if (config.mockRequest.callbackType() == MockRequestCallbackType.FAIL) {
                        config.getListener().onFail(config.mockRequest.result());
                    }
                    config.getListener().requestAfter();
                }
            });
        } else {
            mRequestDelegate.request(config);
        }
    }

    public <S, F> void remove(Request<S, F> config) {
        mRequestDelegate.remove(config);
        if (canPrintLog() && config != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("[remove]")
                    .append("url：")
                    .append(config.getUrl())
                    .append("，type：")
                    .append(config.requestType.name())
                    .append("，TAG：")
                    .append(config.getTAG())
                    .append("，uuid：")
                    .append(config.getUUid());
            logD(builder.toString());
        }
    }

    public void removeByTag(String TAG) {
        mRequestDelegate.removeByTag(TAG);
        if (canPrintLog()) {
            StringBuilder builder = new StringBuilder();
            builder.append("[removeByTag]")
                    .append("TAG：")
                    .append(TAG);
            logD(builder.toString());
        }
    }

    public void removeByUUID(String uuid) {
        mRequestDelegate.removeByUUID(uuid);
        if (canPrintLog()) {
            StringBuilder builder = new StringBuilder();
            builder.append("[removeByUUID]")
                    .append("uuid：")
                    .append(uuid);
            logD(builder.toString());
        }
    }

    /**
     * 请求结果监听
     */
    public interface RequestListener {
        /**
         * 成功
         *
         * @param result 回调参数
         */
        void onSuccess(String result);

        /**
         * 失败
         *
         * @param result 回调参数
         */
        void onFail(String result);
    }

    /**
     * 请求结果监听（带请求开始和结束回调）
     */
    public interface ConverterListener<S, F> {
        /**
         * 成功
         *
         * @param result 回调参数
         */
        void onSuccess(S result);

        /**
         * 失败
         *
         * @param result 回调参数
         */
        void onFail(F result);

    }

    /**
     * 请求结果监听（带请求开始和结束回调）
     */
    public interface WholeConverterListener<S, F> extends ConverterListener<S, F> {
        /**
         * 请求开始回调
         */
        void requestBefore();

        /**
         * 请求结束回调
         */
        void requestAfter();
    }

    /**
     * 请求结果监听（带请求开始和结束回调）
     */
    public interface WholeRequestListener extends RequestListener {
        /**
         * 请求开始回调
         */
        void requestBefore();

        /**
         * 请求结束回调
         */
        void requestAfter();
    }

    /**
     * 请求结果转换
     */
    public interface TransformListener {
        /**
         * 转换请求结果（可对请求回调参数做包装）
         *
         * @param result 回调参数
         */
        String onTransformResult(String result);

        /**
         * 是否将结果转成成功
         */
        TransformCallbackType callbackType();
    }

    /**
     * 请求结果转换回调类型（可强制将请求结果回调为成功，失败）
     */
    public enum TransformCallbackType {
        /**
         * 成功
         */
        SUCCESS,
        /**
         * 失败
         */
        FAIL,
        /**
         * 默认（跟随网络请求回调）
         */
        DEFAULT
    }

    /**
     * 模拟请求配置
     */
    public interface MockRequest {
        /**
         * 请求时长
         */
        int requestDuration();

        /**
         * 模拟请求结果回调类型
         */
        MockRequestCallbackType callbackType();

        /**
         * 模拟请求结果返回值
         */
        String result();
    }

    /**
     * 模拟请求结果回调类型
     */
    public enum MockRequestCallbackType {
        /**
         * 成功
         */
        SUCCESS,
        /**
         * 失败
         */
        FAIL
    }

    /**
     * 请求代理
     */
    public static class Request<S, F> {
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
        final Schedulers schedulers;

        private Request(Builder<S, F> builder) {
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
            if (builder.schedulers == null) {
                schedulers = Looper.myLooper() == Looper.getMainLooper() ? Schedulers.MAIN : Schedulers.DEFAULT;
            } else {
                schedulers = builder.schedulers;
            }
            if (requestListener != null || converterListener != null) {
                Handler dealWithHandler = Looper.myLooper() == Looper.getMainLooper() || getSchedulers() == Schedulers.MAIN ? EasyRequest.getInstance().getHandler() : null;
                realRequestListener = new WholeRequestListener() {
                    @Override
                    public void requestBefore() {
                        if (dealWithHandler != null) {
                            dealWithHandler.post(new Runnable() {
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
                        if (dealWithHandler != null) {
                            dealWithHandler.post(new Runnable() {
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
                        if (dealWithHandler != null) {
                            dealWithHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (transformListener != null) {
                                        String realResult = transformListener.onTransformResult(result);
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
                                String realResult = transformListener.onTransformResult(result);
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
                        if (dealWithHandler != null) {
                            dealWithHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (transformListener != null) {
                                        String realResult = transformListener.onTransformResult(result);
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
                                String realResult = transformListener.onTransformResult(result);
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
         * @deprecated 推荐使用 {@link Request#getMethod()}
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
                builder.append(host);
            }
            if (!TextUtils.isEmpty(path)) {
                builder.append(path);
            }
            return builder.toString();
        }

        public Schedulers getSchedulers() {
            return schedulers;
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
            Schedulers schedulers;

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

            public Builder<S, F> callbackOn(Schedulers schedulers) {
                this.schedulers = schedulers;
                return this;
            }

            public Request<S, F> build() {
                return new Request<>(this);
            }
        }


    }

    public static class RequestParam {
        private Map<String, Object> param;

        public RequestParam() {
            param = new HashMap<>();
        }

        public RequestParam put(String key, Object value) {
            param.put(key, value);
            return this;
        }

        public Map<String, Object> build() {
            return param;
        }
    }

    /**
     * 请求方法
     *
     * @deprecated 推荐使用 {@link Method}
     */
    public enum RequestType {
        GET,
        POST,
        PUT,
        DELETE
    }

    /**
     * 请求方法
     */
    public enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }

    /**
     * 回调线程
     */
    public enum Schedulers {
        /**
         * 默认（跟随发起请求的线程）
         */
        DEFAULT,
        /**
         * 主线程
         */
        MAIN
    }

    /**
     * 请求代理
     */
    public interface RequestDelegate {
        /**
         * 发起请求
         */
        <S, F> void request(Request<S, F> config);

        /**
         * 移除请求
         */
        <S, F> void remove(Request<S, F> config);

        /**
         * 通过tag移除请求
         */
        void removeByTag(String TAG);

        /**
         * 通过uuid移除请求
         */
        void removeByUUID(String uuid);
    }

    /**
     * 日志输出接口
     */
    public interface ILog {
        void onLogVerbose(String TAG, String log);

        void onLogDebug(String TAG, String log);

        void onLogInfo(String TAG, String log);

        void onLogWarn(String TAG, String log);

        void onLogError(String TAG, String log);
    }

}
