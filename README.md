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
	        implementation 'com.github.linpeixu:EasyRequest:1.1.17'
            //或者implementation 'com.gitlab.linpeixu:easyrequest:1.1.17'
	}
```

若编译过程中报Duplicate class错误，可能是你的项目也引入了Okhttp，尝试在module下的gradle文件下
的android标签里加入如下：

```java
    configurations {
            cleanedAnnotations
            compile.exclude group: 'com.squareup.okio' , module:'okio'
            compile.exclude group: 'com.squareup.okhttp3' , module:'okhttp'
    }
```
若还有报其它Duplicate class错误，则根据具体的报错信息添加对应的configurations即可。

添加支持Gson解析Json的步骤
Step 1.
```java
    dependencies {
	        implementation 'com.gitlab.linpeixu:GsonConverterFactory:1.0.4 '
            //或者implementation 'com.github.linpeixu:GsonConverterFactory:1.0.4'
	}
```
Step 2.
发起网络请求设置的回调listener传ConverterListener<S,F>（S和F为泛型，分别表示请求成功和失败结果经Gson解析后
的对象类型），假设我们需将成功的结果转为自定义的Person，则使用实例如下：
```java
EasyRequest.getInstance().request(new BaseRequest.Builder<Person,String>()
                    .method(EasyRequest.Method.POST)
                    .name("user")//设置微服务名，对应的host在NetworkConfig设置server时设置
                    .path("login/")
                    .params(new RequestParam()
                            .put("mobile", "13000000000")
                            .put("password", "123456")
                            .build())
                    //添加Json转换工厂，具体可传的参数详见GsonConverterFactory工程
                    .addConverterFactory(new TypeClassConverterFactory<Person, String>() {
                    @Override
                    public Class<String> getFailClassType() {
                        return String.class;
                    }

                    @Override
                    public Type getSuccessType() {
                        return new TypeToken<Person>() {
                        }.getType();
                    }
                    })
                    .listener(new ConverterListener<Person,String>() {
                        @Override
                        public void onSuccess(Person result) {
                           /*请求成功回调，result为接口返回的json数据经Gson解析后的对象*/
                        }

                        @Override
                        public void onFail(String result) {
                            /*请求失败回调，result为接口返回的数据经Gson解析后的对象*/
                        }
                    })
                    .build());
```


之前写过一篇[《Android快速集成网络库功能》](https://www.toutiao.com/i6921606978686943752/)，思前想后感觉还是有点局限性，只限于接口端采用微服务架构且app端采用retrofit+okhttp+rxjava的网络框架，这对其它对接入单服务接口类型的app端，或者是采用其它网络框架的app端就不适用了，因此就有了这篇文章。
![在这里插入图片描述](https://img-blog.csdnimg.cn/810e7dff300441938bbd685552b62ad7.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA54mnLueJpw==,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

我们还是采用跟[《Android快速集成图片加载（支持更换图片加载库）》](https://www.toutiao.com/i6919748293425791500/)一样的方案，可以使用不用的网络请求策略，才实现支持更换网络加载库，最终发起一个请求只需要一行代码，比如：

微服务架构的接口：

```java
EasyRequest.getInstance().request(new BaseRequest.Builder<String,String>()
                    .method(Method.POST)
                    .name("user")//设置微服务名，对应的host在NetworkConfig设置server时设置
                    .path("login/")
                    .params(new RequestParam()
                            .put("mobile", "13000000000")
                            .put("password", "123456")
                            .build())
                    .listener(new RequestListener() {
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
EasyRequest.getInstance().request(DefaultRequest.create()
                    .method(Method.POST)
                    .host("https://api.test.com/")//这里需要说明的是，不设置host时框架会去读取NetworkConfig设置的service的host
                    .path("login/")
                    .params(new RequestParam()
                            .put("mobile", "13000000000")
                            .put("password", "123456")
                            .build())
                    .listener(new RequestListener() {
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
EasyRequest.getInstance().request(DefaultRequest.create()
                .method(Method.POST)
                .host("https://api.test.com/")
                .path("login/")
                .params(new RequestParam()
                        .put("mobile", "13000000000")
                        .put("password", "123456")
                        .build())
                .transformResult(new TransformListener() {
                    @Override
                    public String onTransformResult(String result) {
                        /*转换网络请求返回的数据*/
                        return "{\"status\":-1,\"message\":\"请求失败\"}";
                    }

                    @Override
                    public TransformCallbackType callbackType() {
                        /*将网络请求结果强制置为失败*/
                        return TransformCallbackType.FAIL;
                    }
                })
                .listener(new RequestListener() {
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
2、请求结果转换回调方法支持带原始的回调类型（网络请求回调成功或失败）；

只需设置transformResult时传OriginalTransformListener，如下
```java
EasyRequest.getInstance().request(DefaultRequest.create()
                .method(Method.POST)
                .host("https://api.test.com/")
                .path("login/")
                .params(new RequestParam()
                        .put("mobile", "13000000000")
                        .put("password", "123456")
                        .build())
                .transformResult(new OriginalTransformListener() {
                    @Override
                    public String onTransformResult(OriginalCallback type,String result) {
                        if(type == OriginalCallback.FAILURE){
                          /*转换网络请求返回的数据*/
                          return "{\"status\":-1,\"message\":\"请求失败\"}";
                        }
                        return result;
                    }

                    @Override
                    public TransformCallbackType callbackType() {
                        return TransformCallbackType.DEFAULT;
                    }
                })
                .listener(new RequestListener() {
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

OriginalTransformListener.java

```java
package com.cloudling.request.network;

import com.cloudling.request.listener.TransformListener;
import com.cloudling.request.type.OriginalCallback;

/**
 * 描述: 请求结果转换（带原始的回调类型（网络请求回调成功或失败））
 * 联系：1966353889@qq.com
 * 日期: 2021/12/30
 */
public abstract class OriginalTransformListener implements TransformListener {
    public abstract String onTransformResult(OriginalCallback type, String result);

    @Override
    public final String onTransformResult(String result) {
        return null;
    }
}

```

OriginalCallback.java

```java
package com.cloudling.request.type;

/**
 * 描述: 原始的回调类型（网络请求回调成功或失败）
 * 作者: 1966353889@qq.com
 * 日期: 2021/12/30
 */
public enum OriginalCallback {
    /**
     * 成功
     */
    SUCCESS,
    /**
     * 失败
     */
    FAILURE
}

```
3、模拟网络请求
这里需要注意的是，配置模拟网络请求实际上不会发起网络请求，而是直接回调，且优先级大于transformResult
```java
EasyRequest.getInstance().request(DefaultRequest.create()
                .method(Method.POST)
                .host("https://api.test.com/")
                .path("login/")
                .params(new RequestParam()
                        .put("mobile", "13000000000")
                        .put("password", "123456")
                        .build())
                .mockRequest(new MockRequest() {
                    @Override
                    public int requestDuration() {
                        /*配置模拟网络请求的时长*/
                       return 1;
                    }

                    @Override
                    public MockRequestCallbackType callbackType() {
                        /*配置模拟网络请求成功*/
                        return MockRequestCallbackType.SUCCESS;
                    }

                    @Override
                    public String result() {
                        /*配置模拟网络请求返回的数据*/
                        return "{\"status\":0,\"message\":\"请求成功\"}";
                    }
                }).listener(new RequestListener() {
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
package com.cloudling.request.delegate;

import android.text.TextUtils;
import android.util.ArrayMap;
import com.cloudling.request.network.BaseRequest;
import com.cloudling.request.network.EasyRequest;
import com.cloudling.request.network.NetworkConfig;
import com.cloudling.request.type.Method;
import com.cloudling.request.type.RequestType;
import org.apache.http.conn.ConnectTimeoutException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import java.io.IOException;
import java.net.SocketTimeoutException;
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
                    String methodName = config.getMethod() != null ? config.getMethod().name() : (config.getType() != null ? config.getType().name() : null);
                    config.getListener().onFail("未知的请求方法-" + methodName);
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
                String result;
                if (e instanceof SocketTimeoutException) {
                    /*响应超时*/
                    result = "SocketTimeoutException";
                } else if (e instanceof ConnectTimeoutException) {
                    /*请求超时*/
                    result = "ConnectTimeoutException";
                } else {
                    result = e.toString();
                }
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
 * 联系: 1966353889@qq.com
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
EasyRequest.getInstance().request(DefaultRequest.create()
                    .method(Method.POST)
                    .name("user")//设置微服务名，对应的host在NetworkConfig设置server时设置
                    .path("login/")
                    .params(new RequestParam()
                            .put("mobile", "13000000000")
                            .put("password", "123456")
                            .build())
                    .listener(new RequestListener() {
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
EasyRequest.getInstance().request(DefaultRequest.create()
                    .method(Method.POST)
                    .host("https://api.test.com/")
                    .path("login/")
                    .params(new RequestParam()
                            .put("mobile", "13000000000")
                            .put("password", "123456")
                            .build())
                    .listener(new RequestListener() {
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
package com.cloudling.request.network;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.cloudling.request.BuildConfig;
import com.cloudling.request.delegate.OkHttpDelegate;
import com.cloudling.request.delegate.RequestDelegate;
import com.cloudling.request.listener.ILog;
import com.cloudling.request.type.MockRequestCallbackType;
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
            mRequestDelegate.request(config);
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

```
RequestListener.java

```java
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
```
WholeRequestListener.java

```java
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
```
TransformListener.java

```java
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
```
TransformCallbackType.java

```java
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
```
MockRequest.java

```java
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
```
MockRequestCallbackType.java

```java
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
```
BaseRequest.java

```java
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

```
RequestParam.java

```java
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
```
RequestType.java

```java
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
```
Method.java

```java
    /**
     * 请求方法
     */
    public enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }
```
RequestDelegate.java

```java
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
```
ILog.java

```java
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
```

BaseService.java

```java
import android.text.TextUtils;
import android.util.ArrayMap;

/**
 * 描述: 接口请求服务基类
 * 联系: 1966353889@qq.com
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

