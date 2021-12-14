package com.cloudling.request.listener;


/**
 * 描述: 接口超时配置
 * 联系：1966353889@qq.com
 * 日期: 2021/12/3
 */
public interface TimeoutListener {
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