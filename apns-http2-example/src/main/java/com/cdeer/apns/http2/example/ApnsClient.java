package com.cdeer.apns.http2.example;


import com.cdeer.apns.http2.core.error.ErrorDispatcher;
import com.cdeer.apns.http2.core.error.ErrorListener;
import com.cdeer.apns.http2.core.error.ErrorModel;
import com.cdeer.apns.http2.core.manager.ApnsServiceManager;
import com.cdeer.apns.http2.core.model.ApnsConfig;
import com.cdeer.apns.http2.core.model.Payload;
import com.cdeer.apns.http2.core.model.PushNotification;
import com.cdeer.apns.http2.core.netty.http2.HttpResponseHandler;
import com.cdeer.apns.http2.core.service.ApnsService;
import com.cdeer.apns.http2.core.service.NettyApnsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class ApnsClient {

    private static final Logger log = LoggerFactory.getLogger(ApnsClient.class);

    private static final String TOKEN = "3e1fe6153769a7abc922d1cb80e676ec835d01c5bd316ab1e7493321aafde111";

    public static void main(String[] args) {
        // 读取证书
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("zhengshu.p12");

        ApnsConfig config = new ApnsConfig();
        config.setName("name1");// 推送服务名称
        config.setDevEnv(false);// 是否是开发环境
        config.setKeyStore(is);// 证书
        config.setPassword("111111");// 证书密码
        config.setPoolSize(1);// 线程池大小
        config.setTimeout(3000);// TCP连接超时时间
        config.setTopic("com.push");// 标题,即证书的bundleID
        ApnsServiceManager.createService(config);


        ErrorDispatcher.getInstance().addListener(new ErrorListener() {
            @Override
            public void handle(ErrorModel errorModel) {
                log.info("收到错误监听:" + errorModel);

            }
        });

        sendNotification(config.getName(), TOKEN);

    }

    public static void sendNotification(String appName, String token) {

        Payload payload = new Payload();
        payload.setAlert("test");

        PushNotification notification = new PushNotification();
        notification.setPayload(payload);
        notification.setToken(token);

        ApnsService service = ApnsServiceManager.getService(appName);
        service.sendNotification(notification);// 异步发送
//        boolean result = service.sendNotificationSynch(notification);// 同步发送
    }

}
