先看接入步骤：
Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```java
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Step 2. Add the dependency
```java
    dependencies {
	        implementation 'com.gitlab.linpeixu:easyrequest:1.1.7'
	}
```
之前写过一篇[《Android快速集成网络库功能》](https://www.toutiao.com/i6921606978686943752/)，思前想后感觉还是有点局限性，只限于接口端采用微服务架构且app端采用retrofit+okhttp+rxjava的网络框架，这对其它对接入单服务接口类型的app端，或者是采用其它网络框架的app端就不适用了，因此就有了这篇文章。
![在这里插入图片描述](https://img-blog.csdnimg.cn/810e7dff300441938bbd685552b62ad7.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA54mnLueJpw==,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

我们还是采用跟[《Android快速集成图片加载（支持更换图片加载库）》](https://www.toutiao.com/i6919748293425791500/)一样的方案，可以使用不用的网络请求策略，才实现支持更换网络加载库，最终发起一个请求只需要一行代码，比如：

微服务架构的接口：

```java
EasyRequest.getInstance().request(new EasyRequest.Request.Builder()
                    .type(EasyRequest.RequestType.POST)
                    .name("user")//设置微服务名，对应的host在NetworkConfig设置server时设置
                    .path("login/")
                    .params(new EasyRequest.RequestParam()
                            .put("mobile", "13000000000")
                            .put("password", "123456")
                            .build())
                    .listener(new EasyRequest.RequestListener() {
                        @Override
                        public void onSuccess(String result) {
                           /*请求成功回调，result为接口返回的json数据*/
                        }

                        @Override
                        public void onFail(String result) {
                            /*请求失败回调，result为接口返回的数据*/
                        }
                    })
                    .build());
```

单服务架构的接口：

```java
EasyRequest.getInstance().request(new EasyRequest.Request.Builder()
                    .type(EasyRequest.RequestType.POST)
                    .host("https://api.test.com/")
                    .path("login/")
                    .params(new EasyRequest.RequestParam()
                            .put("mobile", "13000000000")
                            .put("password", "123456")
                            .build())
                    .listener(new EasyRequest.RequestListener() {
                        @Override
                        public void onSuccess(String result) {
                           /*请求成功回调，result为接口返回的json数据*/
                        }

                        @Override
                        public void onFail(String result) {
                            /*请求失败回调，result为接口返回的数据*/
                        }
                    })
                    .build());
```

在任何你想取消一个请求的时候还可通过以下方法来做到：

```java
/*config为EasyRequest.getInstance().request的传参*/
EasyRequest.getInstance().remove(config);
```

```java
/*tag为创建config设置的tag*/
EasyRequest.getInstance().removeByTag(tag);
```

```java
/*uuid为config初始化时自动设置的uuid*/
EasyRequest.getInstance().removeByUUID(uuid);
```
扩展
1、我们支持将网络请求的回调进行转换，比如将成功的网络请求强制转为失败、将网络请求返回的json提前处理等，示例如下
```java
EasyRequest.getInstance().request(new EasyRequest.Request.Builder()
                .type(EasyRequest.RequestType.POST)
                .host("https://api.test.com/")
                .path("login/")
                .params(new EasyRequest.RequestParam()
                        .put("mobile", "13000000000")
                        .put("password", "123456")
                        .build())
                .transformResult(new EasyRequest.TransformListener() {
                    @Override
                    public String onTransformResult(String result) {
                        /*转换网络请求返回的数据*/
                        return "{\"status\":-1,\"message\":\"请求失败\"}";
                    }

                    @Override
                    public EasyRequest.TransformCallbackType callbackType() {
                        /*将网络请求结果强制置为失败*/
                        return EasyRequest.TransformCallbackType.FAIL;
                    }
                })
                .listener(new EasyRequest.RequestListener() {
                    @Override
                    public void onSuccess(String result) {
                        /*请求成功回调，result为接口返回的json数据*/
                    }

                    @Override
                    public void onFail(String result) {
                        /*请求失败回调，result为接口返回的数据*/
                    }
                })
                .build());
```
2、模拟网络请求
这里需要注意的是，配置模拟网络请求实际上不会发起网络请求，而是直接回调，且优先级大于transformResult
```java
EasyRequest.getInstance().request(new EasyRequest.Request.Builder()
                .type(EasyRequest.RequestType.POST)
                .host("https://api.test.com/")
                .path("login/")
                .params(new EasyRequest.RequestParam()
                        .put("mobile", "13000000000")
                        .put("password", "123456")
                        .build())
                .mockRequest(new EasyRequest.MockRequest() {
                    @Override
                    public int requestDuration() {
                        /*配置模拟网络请求的时长*/
                        /*需要注意的是如果EasyRequest没有设置setHandlerForMockRequest，则这里的时长不会生效*/
                        return 1;
                    }

                    @Override
                    public EasyRequest.MockRequestCallbackType callbackType() {
                        /*配置模拟网络请求成功*/
                        return EasyRequest.MockRequestCallbackType.SUCCESS;
                    }

                    @Override
                    public String result() {
                        /*配置模拟网络请求返回的数据*/
                        return "{\"status\":0,\"message\":\"请求成功\"}";
                    }
                }).listener(new EasyRequest.RequestListener() {
                    @Override
                    public void onSuccess(String result) {
                        /*请求成功回调，result为接口返回的json数据*/
                    }

                    @Override
                    public void onFail(String result) {
                        /*请求失败回调，result为接口返回的数据*/
                    }
                })
                .build());
```

这里我们默认实现了OkHttp的请求代理，如通过其它网路库的可自行实现RequestDelegate，然后调用下边的方法设置网路请求代理：

```java
EasyRequest.getInstance().setRequestDelegate(requestDelegate);
```

自行实现请求代理的，remove方法的实现可参考我们默认的OkHttp请求代理，我们看下OkHttpDelegate的完整代码：

```java
import android.text.TextUtils;
import android.util.ArrayMap;
import com.cloudling.request.EasyRequest;
import com.cloudling.request.NetworkConfig;
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
public class OkHttpDelegate implements EasyRequest.RequestDelegate {
    /**
     * okHttp请求实例
     */
    private OkHttpClient mOkHttpClient;
    /**
     * 缓存请求
     */
    private ConcurrentHashMap<String, EasyRequest.Request> mRequestMap;

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
    public void request(EasyRequest.Request config) {
        if (config == null) {
            return;
        }
        mRequestMap.put(config.getUUid(), config);
        if (config.getListener() instanceof EasyRequest.WholeRequestListener
                && mRequestMap.get(config.getUUid()) != null) {
            ((EasyRequest.WholeRequestListener) config.getListener()).requestBefore();
        }
        Request.Builder builder = new Request.Builder();
        if (config.getType() == EasyRequest.RequestType.GET) {
            if (config.getParams() != null) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(config.getUrl()).newBuilder();
                for (Map.Entry<String, Object> entry : config.getParams().entrySet()) {
                    urlBuilder.addQueryParameter(entry.getKey(), String.valueOf(entry.getValue()));
                }
                builder.url(urlBuilder.build());
            } else {
                builder.url(config.getUrl());
            }
        } else if (config.getType() == EasyRequest.RequestType.POST
                || config.getType() == EasyRequest.RequestType.PUT
                || config.getType() == EasyRequest.RequestType.DELETE) {
            builder.url(config.getUrl());
            RequestBody body;
            if (config.getParams() != null) {
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                String json = new JSONObject(config.getParams()).toString();
                body = RequestBody.create(mediaType, json);
            } else {
                body = RequestBody.create(null, "");
            }
            if (config.getType() == EasyRequest.RequestType.POST) {
                builder.post(body);
            } else if (config.getType() == EasyRequest.RequestType.PUT) {
                builder.put(body);
            } else if (config.getType() == EasyRequest.RequestType.DELETE) {
                builder.delete(body);
            }
        } else {
            EasyRequest.getInstance().logE("未知的请求方法-" + config.getType().name());
            if (mRequestMap.get(config.getUUid()) != null) {
                if (config.getListener() != null) {
                    config.getListener().onFail("未知的请求方法");
                }
                if (config.getListener() instanceof EasyRequest.WholeRequestListener) {
                    ((EasyRequest.WholeRequestListener) config.getListener()).requestAfter();
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
                    EasyRequest.getInstance().logE("[onFailure]result：" + result);
                    if (config.getListener() != null) {
                        config.getListener().onFail(e.toString());
                    }
                    if (config.getListener() instanceof EasyRequest.WholeRequestListener) {
                        ((EasyRequest.WholeRequestListener) config.getListener()).requestAfter();
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
                    EasyRequest.getInstance().logI("[onResponse]result：" + result);
                    if (config.getListener() != null) {
                        config.getListener().onSuccess(result);
                    }
                    if (config.getListener() instanceof EasyRequest.WholeRequestListener) {
                        ((EasyRequest.WholeRequestListener) config.getListener()).requestAfter();
                    }
                    removeByUUID(config.getUUid());
                } else {
                    EasyRequest.getInstance().logI("[onResponse-no callback]result：" + result);
                }
            }
        });

    }

    @Override
    public void remove(EasyRequest.Request config) {
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

```

设置对应的log处理类

```java
EasyRequest.getInstance().setILog(ILog log);
```

设置是否打开调试模式

```java
EasyRequest.getInstance().enableDebug(boolean debug);
```
同样的我们通过NetworkConfig来配置全局的请求头，超时时间，接口请求服务类（可根据需要设置对应的服务类，如各种开发环境下的请求服务类等），我们来看NetworkConfig的完整代码：

```java
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * 描述: 网络请求配置类
 * 作者: lpx
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
```


由于之前在开发过程中，我们在app的启动页通过NetworkConfig配置了接口环境为线上正式环境，在MainActivity也处理了页面异常关闭重新创建时的页面数据的恢复处理，这个时候我们不会再重新走一遍启动页，所以接口环境就有问题了，所以我们在NetworkConfig中提供了两个方法去处理这种情况，分别为onSaveInstanceState(Bundle outState)和onCreate(Bundle outState)方法。

使用方法：

在需要的Activity中的onCreate方法中调用

`NetworkConfig.getInstance().onCreate(savedInstanceState`);

在需要的Activity中的onSaveInstanceState方法中调用

```java
NetworkConfig.getInstance().onSaveInstanceState(outState);
```

这样就可以自动恢复我们配置好的各种网络配置参数，感兴趣的可以去看NetworkConfig的实现。

采用本文的网络请求架构来实现发起网络请求的完整使用步骤如下：

1、通过NetworkConfig配置接口请求所需各种参数（一般可在application或启动页配置）

微服务接口架构配置示例：

```java
NetworkConfig.getInstance().setService(new DefaultMicroService() {
            @Override
            public ArrayMap<String, String> setMicroConfig() {
                ArrayMap<String, String> config = new ArrayMap<>();
                config.put("user", "https://user-api.test.com/");
                config.put("game", "https://game-api.test.com/");
                config.put("account", "https://account-api.test.com/");
               return config;
            }

            @Override
            public ArrayMap<String, Object> setHeaders() {
                ArrayMap<String, Object> headers = new ArrayMap<>();
                headers.put("header1", "value1");
                headers.put("header2", "value2");
                ...
                return headers;
            }
        });
```

单服务接口架构配置示例：

```java
NetworkConfig.getInstance().setService(new DefaultSingleService() {
            @Override
            public String setHost() {
                return “https://api.test.com/”;
            }

            @Override
            public ArrayMap<String, Object> setHeaders() {
                 ArrayMap<String, Object> headers = new ArrayMap<>();
                headers.put("header1", "value1");
                headers.put("header2", "value2");
                ...
                return headers;
            }
        });
```

不同开发环境可通过setService设置带不同host的Service来实现

2、发起请求

微服务接口架构请求示例：

```java
EasyRequest.getInstance().request(new EasyRequest.Request.Builder()
                    .type(EasyRequest.RequestType.POST)
                    .name("user")//设置微服务名，对应的host在NetworkConfig设置server时设置
                    .path("login/")
                    .params(new EasyRequest.RequestParam()
                            .put("mobile", "13000000000")
                            .put("password", "123456")
                            .build())
                    .listener(new EasyRequest.RequestListener() {
                        @Override
                        public void onSuccess(String result) {
                           /*请求成功回调，result为接口返回的json数据*/
                        }

                        @Override
                        public void onFail(String result) {
                            /*请求失败回调，result为接口返回的数据*/
                        }
                    })
                    .build());
```

单服务接口架构请求示例：

```java
EasyRequest.getInstance().request(new EasyRequest.Request.Builder()
                    .type(EasyRequest.RequestType.POST)
                    .host("https://api.test.com/")
                    .path("login/")
                    .params(new EasyRequest.RequestParam()
                            .put("mobile", "13000000000")
                            .put("password", "123456")
                            .build())
                    .listener(new EasyRequest.RequestListener() {
                        @Override
                        public void onSuccess(String result) {
                           /*请求成功回调，result为接口返回的json数据*/
                        }

                        @Override
                        public void onFail(String result) {
                            /*请求失败回调，result为接口返回的数据*/
                        }
                    })
                    .build());
```

这样就可以了，架构涉及到的其它java完整代码如下：

EasyRequest.java

```java
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
     * 设置是否打开调试模式
     */
    public void enableDebug(boolean debug) {
        this.debug = debug;
        if (canPrintLog()) {
            logD("[enableDebug]");
        }
    }

    /**
     * 是否可输出调试日志
     */
    public boolean canPrintLog() {
        return debug && mILog != null;
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

    public void request(Request config) {
        mRequestDelegate.request(config);
        if (canPrintLog() && config != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("[request]")
                    .append("url：")
                    .append(config.getUrl())
                    .append("，type：")
                    .append(config.requestType.name())
                    .append("，params：")
                    .append(new JSONObject(config.params).toString());
            logD(builder.toString());
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
         * 请求回调
         */
        RequestListener listener;
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
            listener = builder.listener;
            host = builder.host;
            path = builder.path;
            name = builder.name;
            TAG = builder.TAG;
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

        public RequestListener getListener() {
            return listener;
        }

        public String getUrl() {
            StringBuilder builder = new StringBuilder();
            if (!TextUtils.isEmpty(name)) {
                builder.append(NetworkConfig.getInstance().getHost(name));
            } else {
                builder.append(host);
            }
            builder.append(path);
            return builder.toString();
        }

        public static class Builder {
            RequestType requestType;
            Map<String, Object> params;
            RequestListener listener;
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
                this.listener = listener;
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
```

BaseService.java

```java
import android.text.TextUtils;
import android.util.ArrayMap;

/**
 * 描述: 接口请求服务基类
 * 作者: lpx
 * 日期: 2021/12/3
 */
abstract class BaseService implements TimeoutListener {
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
    long getConnectTimeout() {
        return mConnectTimeout;
    }

    /**
     * 获取读取超时时间
     */
    long getReadTimeout() {
        return mReadTimeout;
    }

    /**
     * 获取写入超时时间
     */
    long getWriteTimeout() {
        return mWriteTimeout;
    }

    /**
     * 获取host（单服务）
     */
    String getHost() {
        return mHost;
    }

    /**
     * 获取host（微服务）
     *
     * @param name 微服务服务名
     */
    String getHost(String name) {
        return mMicroConfig != null && !TextUtils.isEmpty(name) ? mMicroConfig.get(name) : null;
    }

    /**
     * 获取微服务配置
     */
    ArrayMap<String, String> getMicroConfig() {
        return mMicroConfig;
    }

    /**
     * 获取请求头
     */
    ArrayMap<String, Object> getHeaders() {
        return mHeaders;
    }

    /**
     * 设置请求头
     */
    void headers(ArrayMap<String, Object> headers) {
        mHeaders = headers;
    }

    /**
     * 设置请求头
     */
    void header(String key, Object value) {
        if (mHeaders == null) {
            mHeaders = new ArrayMap<>();
        }
        mHeaders.put(key, value);
    }
}
```

BaseSingleService.java

```java
import android.util.ArrayMap;

/**
 * 描述: 单服务接口请求服务基类（不同环境继承该类设置不同的host）
 * 作者：lpx
 * 日期: 2021/12/3
 */
public abstract class BaseSingleService extends BaseService {
    public BaseSingleService() {
        super();
    }

    @Override
    public final ArrayMap<String, String> setMicroConfig() {
        return null;
    }

}
```

BaseMicroService.java

```java
/**
 * 描述: 微服务接口请求服务基类（不同环境继承该类设置不同的host）
 * 作者：lpx
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
```

DefaultSingleService.java

```java
import android.util.ArrayMap;

/**
 * 描述: 单服务接口默认请求服务基类（已实现默认超时时间，不同环境继承该类设置不同的host）
 * 作者：lpx
 * 日期: 2021/12/3
 */
public abstract class DefaultSingleService extends BaseService {
    public DefaultSingleService() {
        super();
    }

    @Override
    public final ArrayMap<String, String> setMicroConfig() {
        return null;
    }

    @Override
    public final long connectTimeout() {
        return 20;
    }

    @Override
    public final long readTimeout() {
        return 20;
    }

    @Override
    public final long writeTimeout() {
        return 20;
    }
}
```

DefaultMicroService.java

```java
/**
 * 描述: 微服务接口默认请求服务基类（已实现默认超时时间，不同环境继承该类设置不同的host）
 * 作者：lpx
 * 日期: 2021/12/3
 */
public abstract class DefaultMicroService extends BaseService {
    public DefaultMicroService() {
        super();
    }

    @Override
    public final String setHost() {
        return null;
    }

    @Override
    public final long connectTimeout() {
        return 20;
    }

    @Override
    public final long readTimeout() {
        return 20;
    }

    @Override
    public final long writeTimeout() {
        return 20;
    }
}
```

SupportCallback.java

```java
import android.os.Bundle;
import android.util.ArrayMap;

/**
 * 描述: 接口请求支持回调
 * 作者：lpx
 * 日期: 2021/12/3
 */
interface SupportCallback {
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
```

TimeoutListener.java

```java
/**
 * 描述: 接口超时配置
 * 作者：lpx
 * 日期: 2021/12/3
 */
interface TimeoutListener {
    /**
     * 连接超时时间
     */
    long connectTimeout();

    /**
     * 读取超时时间
     */
    long readTimeout();

    /**
     * 写入超时时间
     */
    long writeTimeout();

}
```

是不是很方便快捷，希望本文可以帮助到您，也希望各位不吝赐教，提出您在使用中的宝贵意见，谢谢。

![在这里插入图片描述](https://img-blog.csdnimg.cn/cbc7403dc04040c5823f0092d062c9e5.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA54mnLueJpw==,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

