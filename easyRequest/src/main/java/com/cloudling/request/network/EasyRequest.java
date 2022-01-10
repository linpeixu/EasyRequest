package com.cloudling.request.network;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.cloudling.request.BuildConfig;
import com.cloudling.request.cache.AesHelper;
import com.cloudling.request.cache.CacheHelper;
import com.cloudling.request.cache.CacheType;
import com.cloudling.request.cache.ReadCacheType;
import com.cloudling.request.delegate.OkHttpDelegate;
import com.cloudling.request.delegate.RequestDelegate;
import com.cloudling.request.listener.ILog;
import com.cloudling.request.type.MockRequestCallbackType;
import com.cloudling.request.type.OriginalCallback;

import org.json.JSONArray;
import org.json.JSONObject;

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

    public Handler getHandler() {
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

    /**
     * 判断是否为json
     */
    public boolean isJson(String result) {
        if (TextUtils.isEmpty(result)) {
            return false;
        }
        boolean isJsonObject = true;
        boolean isJsonArray = true;
        try {
            JSONObject jsonObject = new JSONObject(result);
        } catch (Exception e) {
            isJsonObject = false;
        }
        try {
            JSONArray jsonArray = new JSONArray(result);
        } catch (Exception e) {
            isJsonArray = false;
        }
        if (!isJsonObject && !isJsonArray) {
            return false;
        }
        return true;
    }

    public <S, F> void request(BaseRequest<S, F> config) {
        if (canPrintLog() && config != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("[request]")
                    .append("url：")
                    .append(config.getUrl())
                    .append("，method：")
                    .append(config.getMethod() != null ? config.getMethod().name() : (config.getType() != null ? config.getType().name() : null))
                    .append("，params：")
                    .append(config.params != null ? new JSONObject(config.params).toString() : null);
            logD(builder.toString());
        }
        if (config != null && config.mockRequest != null && config.getListener() != null) {
            /*模拟请求的不允许缓存数据，因为我们缓存的是真实网络请求返回的数据*/
            config.cacheType = CacheType.NO;
            /*模拟请求的不允许读取缓存数据，因为取的数据是我们自己配置的*/
            config.readCacheType = ReadCacheType.NO;
            delayTask(EasyRequest.getInstance().getHandler(), 0, new Runnable() {
                @Override
                public void run() {
                    config.getListener().requestBefore();
                }
            });
            delayTask(EasyRequest.getInstance().getHandler(), config.mockRequest.requestDuration() * 1000L, new Runnable() {
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
            if (config != null && (config.readCacheType == ReadCacheType.READ_SUCCESS_AT_ONCE || config.readCacheType == ReadCacheType.READ_FAIL_AT_ONCE)) {
                String readKey = AesHelper.encryptAsString(config.getCacheKey() + (config.readCacheType == ReadCacheType.READ_SUCCESS_AT_ONCE ? OriginalCallback.SUCCESS.name() : OriginalCallback.FAILURE.name()));
                String cache = CacheHelper.getInstance().getCache(readKey);
                if (!TextUtils.isEmpty(cache)) {
                    /*不允许缓存数据，因为我们已经直接读取缓存的数据*/
                    config.cacheType = CacheType.NO;
                    delayTask(EasyRequest.getInstance().getHandler(), 0, new Runnable() {
                        @Override
                        public void run() {
                            config.getListener().requestBefore();
                            if (config.readCacheType == ReadCacheType.READ_SUCCESS_AT_ONCE) {
                                config.getListener().onSuccess(cache);
                            } else if (config.readCacheType == ReadCacheType.READ_FAIL_AT_ONCE) {
                                config.getListener().onFail(cache);
                            }
                            config.getListener().requestAfter();
                        }
                    });
                } else {
                    /*无缓存的数据，直接发起网络请求*/
                    mRequestDelegate.request(config);
                }
            } else {
                mRequestDelegate.request(config);
            }
        }
    }

    public <S, F> void remove(BaseRequest<S, F> config) {
        mRequestDelegate.remove(config);
        if (canPrintLog() && config != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("[remove]")
                    .append("url：")
                    .append(config.getUrl())
                    .append("，method：")
                    .append(config.getMethod() != null ? config.getMethod().name() : (config.getType() != null ? config.getType().name() : null))
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

}
