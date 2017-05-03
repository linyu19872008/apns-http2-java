package com.cdeer.apns.http2.core.error;

import java.util.ArrayList;
import java.util.List;

/**
 * 错误分发器
 * Created by jacklin on 2017/4/28.
 */
public class ErrorDispatcher {

    private List<ErrorListener> list = new ArrayList<>();

    private ErrorDispatcher() {
    }

    public static ErrorDispatcher getInstance() {
        return Nested.instance;
    }

    private static class Nested {
        private static ErrorDispatcher instance = new ErrorDispatcher();
    }


    /**
     * 添加监听
     *
     * @param errorListener
     */
    public void addListener(ErrorListener errorListener) {
        list.add(errorListener);
    }

    /**
     * 移除监听
     *
     * @param errorListener
     */
    public void removeListener(ErrorListener errorListener) {
        list.remove(errorListener);
    }

    /**
     * 触发监听
     */
    public void dispatch(ErrorModel errorModel) {
        for (ErrorListener listener : list) {
            listener.handle(errorModel);
        }
    }

}
