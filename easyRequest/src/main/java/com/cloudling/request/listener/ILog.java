package com.cloudling.request.listener;

/**
 * 描述: 日志输出接口
 * 联系：1966353889@qq.com
 * 日期: 2021/12/29
 */
public interface ILog {
    void onLogVerbose(String TAG, String log);

    void onLogDebug(String TAG, String log);

    void onLogInfo(String TAG, String log);

    void onLogWarn(String TAG, String log);

    void onLogError(String TAG, String log);
}
