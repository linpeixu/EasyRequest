package com.cloudling.request;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

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
     * 设置Handler（用于带请求时长的模拟请求）
     */
    public void setHandlerForMockRequest(Handler handler) {
        mHandler = handler;
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

    public void request(Request config) {
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
            config.getListener().requestBefore();
            if (config.mockRequest.requestDuration() > 0 && mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (config.mockRequest.callbackType() == MockRequestCallbackType.SUCCESS) {
                            config.getListener().onSuccess(config.mockRequest.result());
                        } else if (config.mockRequest.callbackType() == MockRequestCallbackType.FAIL) {
                            config.getListener().onFail(config.mockRequest.result());
                        }
                    }
                }, config.mockRequest.requestDuration() * 1000L);
            } else {
                if (config.mockRequest.callbackType() == MockRequestCallbackType.SUCCESS) {
                    config.getListener().onSuccess(config.mockRequest.result());
                } else if (config.mockRequest.callbackType() == MockRequestCallbackType.FAIL) {
                    config.getListener().onFail(config.mockRequest.result());
                }
            }
            config.getListener().requestAfter();
        } else {
            mRequestDelegate.request(config);
        }
    }

    public void remove(Request config) {
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
    public static class Request {
        /**
         * 请求类型
         */
        RequestType requestType;
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

        private Request(Builder builder) {
            uuid = UUID.randomUUID().toString();
            requestType = builder.requestType;
            params = builder.params;
            requestListener = builder.requestListener;
            transformListener = builder.transformListener;
            mockRequest = builder.mockRequest;
            host = builder.host;
            path = builder.path;
            name = builder.name;
            TAG = builder.TAG;
            if (requestListener != null) {
                realRequestListener = new WholeRequestListener() {
                    @Override
                    public void requestBefore() {
                        if (requestListener instanceof WholeRequestListener) {
                            ((WholeRequestListener) requestListener).requestBefore();
                        }
                    }

                    @Override
                    public void requestAfter() {
                        if (requestListener instanceof WholeRequestListener) {
                            ((WholeRequestListener) requestListener).requestAfter();
                        }
                    }

                    @Override
                    public void onSuccess(String result) {
                        if (transformListener != null) {
                            String realResult = transformListener.onTransformResult(result);
                            if (transformListener.callbackType() == TransformCallbackType.DEFAULT
                                    || transformListener.callbackType() == TransformCallbackType.SUCCESS) {
                                requestListener.onSuccess(realResult);
                                EasyRequest.getInstance().logI("[onSuccess]result：" + realResult);
                            } else if (transformListener.callbackType() == TransformCallbackType.FAIL) {
                                requestListener.onFail(transformListener.onTransformResult(result));
                                EasyRequest.getInstance().logE("[onFail]result：" + realResult);
                            }
                        } else {
                            requestListener.onSuccess(result);
                            EasyRequest.getInstance().logI("[onSuccess]result：" + result);
                        }
                    }

                    @Override
                    public void onFail(String result) {
                        if (transformListener != null) {
                            String realResult = transformListener.onTransformResult(result);
                            if (transformListener.callbackType() == TransformCallbackType.DEFAULT
                                    || transformListener.callbackType() == TransformCallbackType.FAIL) {
                                requestListener.onFail(realResult);
                                EasyRequest.getInstance().logE("[onFail]result：" + realResult);
                            } else if (transformListener.callbackType() == TransformCallbackType.SUCCESS) {
                                requestListener.onSuccess(transformListener.onTransformResult(result));
                                EasyRequest.getInstance().logI("[onSuccess]result：" + realResult);
                            }
                        } else {
                            requestListener.onFail(result);
                            EasyRequest.getInstance().logE("[onFail]result：" + result);
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

        public RequestType getType() {
            return requestType;
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

        public static class Builder {
            RequestType requestType;
            Map<String, Object> params;
            RequestListener requestListener;
            TransformListener transformListener;
            MockRequest mockRequest;
            String host;
            String path;
            String name;
            String TAG;

            public Builder type(RequestType requestType) {
                this.requestType = requestType;
                return this;
            }

            public Builder params(Map<String, Object> params) {
                this.params = params;
                return this;
            }

            public Builder listener(RequestListener listener) {
                this.requestListener = listener;
                return this;
            }

            public Builder transformResult(TransformListener listener) {
                this.transformListener = listener;
                return this;
            }

            public Builder mockRequest(MockRequest mockRequest) {
                this.mockRequest = mockRequest;
                return this;
            }

            public Builder host(String host) {
                this.host = host;
                return this;
            }

            public Builder path(String path) {
                this.path = path;
                return this;
            }

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder tag(String TAG) {
                this.TAG = TAG;
                return this;
            }

            public Request build() {
                return new Request(this);
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
     */
    public enum RequestType {
        GET,
        POST,
        PUT,
        DELETE
    }

    /**
     * 请求代理
     */
    public interface RequestDelegate {
        /**
         * 发起请求
         */
        void request(Request config);

        /**
         * 移除请求
         */
        void remove(Request config);

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
