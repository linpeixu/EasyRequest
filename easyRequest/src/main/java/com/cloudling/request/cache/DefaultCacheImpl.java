package com.cloudling.request.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.cloudling.request.type.OriginalCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * 描述: 默认的缓存实现类
 * 联系: 1966353889@qq.com
 * 日期: 2022/1/6
 */
public final class DefaultCacheImpl extends BaseCacheImpl implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences mPreferences;

    public DefaultCacheImpl(Context context) {
        super(context);
        if (mContext != null) {
            mPreferences = mContext.getSharedPreferences("easy_request_cache", Context.MODE_PRIVATE);
            mPreferences.registerOnSharedPreferenceChangeListener(this);
        }
    }

    public DefaultCacheImpl(Context context, int maxCacheSize) {
        super(context, maxCacheSize);
        if (mContext != null) {
            mPreferences = mContext.getSharedPreferences("easy_request_cache", Context.MODE_PRIVATE);
            mPreferences.registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void addCache(OriginalCallback type, String key, String value) {
        if (isCanOption(key, value)) {
            StringBuilder builder = new StringBuilder()
                    .append("{")
                    .append("\"writeTimeMillis\":")
                    .append(System.currentTimeMillis())
                    .append(",\"saveDuration\":")
                    .append(mDefaultDuration)
                    .append(",\"value\":")
                    .append("\"")
                    .append(value)
                    .append("\"}");
            mPreferences.edit().putString(key, builder.toString()).apply();
        }
    }

    @Override
    public void addCache(OriginalCallback type, String key, String value, long duration) {
        if (isCanOption(key, value)) {
            StringBuilder builder = new StringBuilder()
                    .append("{")
                    .append("\"writeTimeMillis\":")
                    .append(System.currentTimeMillis())
                    .append(",\"saveDuration\":")
                    .append(duration)
                    .append(",\"value\":")
                    .append("\"")
                    .append(value)
                    .append("\"}");
            mPreferences.edit().putString(key, builder.toString()).apply();
        }
    }

    @Override
    public void addCache(OriginalCallback type, String key, String value, long duration, TimeUnit timeUnit) {
        if (isCanOption(key, value)) {
            StringBuilder builder = new StringBuilder()
                    .append("{")
                    .append("\"writeTimeMillis\":")
                    .append(System.currentTimeMillis())
                    .append(",\"saveDuration\":")
                    .append(getSaveDuration(duration, timeUnit))
                    .append(",\"value\":")
                    .append("\"")
                    .append(value)
                    .append("\"}");
            mPreferences.edit().putString(key, builder.toString()).apply();
        }
    }

    @Override
    public String getCache(String key) {
        if (isCanOption(key)) {
            String result = mPreferences.getString(key, null);
            if (!TextUtils.isEmpty(result)) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    long timeMillis = jsonObject.optLong("writeTimeMillis") + jsonObject.optLong("saveDuration");
                    if (timeMillis < System.currentTimeMillis()) {
                        /*缓存的数据已过期*/
                        mPreferences.edit().remove(key).apply();
                    } else {
                        return result;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mPreferences.edit().remove(key).apply();
                }
            }
        }
        return null;
    }

    @Override
    public void removeCache(String key) {
        if (isCanOption(key)) {
            mPreferences.edit().remove(key).apply();
        }
    }

    @Override
    public void removeAllCache() {
        if (mPreferences != null) {
            mPreferences.edit().clear().apply();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (!TextUtils.isEmpty(key) && mMaxCacheSize > 0 && preferences != null
                && preferences.getAll() != null
                && preferences.getAll().size() > mMaxCacheSize) {
            Map<String, ?> map = preferences.getAll();
            long minTimeMillis = -1;
            String targetKey = null;
            ArrayList<String> errorList = new ArrayList<>();
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                if (entry.getValue() instanceof String) {
                    try {
                        JSONObject jsonObject = new JSONObject((String) entry.getValue());
                        long writeTimeMillis = jsonObject.optLong("writeTimeMillis");
                        if (minTimeMillis == -1 || writeTimeMillis < minTimeMillis) {
                            minTimeMillis = writeTimeMillis;
                            targetKey = entry.getKey();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        errorList.add(entry.getKey());
                    }
                } else {
                    errorList.add(entry.getKey());
                }
            }
            if (targetKey != null) {
                preferences.edit().remove(targetKey).apply();
            }
            if (errorList.size() > 0) {
                for (int i = 0, size = errorList.size(); i < size; i++) {
                    preferences.edit().remove(errorList.get(i)).apply();
                }
            }
        }
    }

    private boolean isCanOption(String key, String value) {
        return mPreferences != null && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(value);
    }

    private boolean isCanOption(String key) {
        return mPreferences != null && !TextUtils.isEmpty(key);
    }

    private long getSaveDuration(long duration, TimeUnit timeUnit) {
        if (timeUnit != null && duration > 0) {
            if (timeUnit == TimeUnit.SECONDS) {
                return duration * 1000;
            } else if (timeUnit == TimeUnit.MINUTES) {
                return duration * 1000 * 60;
            } else if (timeUnit == TimeUnit.HOURS) {
                return duration * 1000 * 60 * 60;
            } else if (timeUnit == TimeUnit.DAYS) {
                return duration * 1000 * 60 * 60 * 24;
            }
            return duration;
        }
        return mDefaultDuration;
    }
}
