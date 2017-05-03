package com.cdeer.apns.http2.core.error;

import com.alibaba.fastjson.JSON;
import com.cdeer.apns.http2.core.model.PushNotification;

/**
 * 错误模型
 * Created by jacklin on 2017/4/28.
 */
public class ErrorModel {

    private int code;// http2返回的状态码，非200
    private String appName;// app的名称
    private PushNotification notification;// 发送的通知

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public PushNotification getNotification() {
        return notification;
    }

    public void setNotification(PushNotification notification) {
        this.notification = notification;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
