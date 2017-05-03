package com.cdeer.apns.http2.core.error;

/**
 * 错误监听器
 * Created by jacklin on 2017/4/28.
 */
public interface ErrorListener {

    /**
     * 处理错误
     *
     * @param errorModel
     */
    void handle(ErrorModel errorModel);

}
