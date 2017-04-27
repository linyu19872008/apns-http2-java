package com.cdeer.apns.http2.core.service;


import com.cdeer.apns.http2.core.model.ApnsConfig;
import com.cdeer.apns.http2.core.model.PushNotification;
import com.cdeer.apns.http2.core.netty.NettyApnsConnection;
import com.cdeer.apns.http2.core.netty.NettyApnsConnectionPool;

public class NettyApnsService extends AbstractApnsService {

    private NettyApnsConnectionPool connectionPool;

    private NettyApnsService(ApnsConfig config) {
        super(config);
        connectionPool = new NettyApnsConnectionPool(config);
    }

    public static NettyApnsService create(ApnsConfig apnsConfig) {
        return new NettyApnsService(apnsConfig);
    }

    @Override
    public void sendNotification(PushNotification notification) {
        executorService.execute(() -> {
            NettyApnsConnection connection = null;
            try {
                connection = connectionPool.acquire();
                if (connection != null) {
                    connection.sendNotification(notification);
                }
            } catch (Exception e) {
                log.error("sendNotification", e);
            } finally {
                connectionPool.release(connection);
            }
        });
    }

    @Override
    public void shutdown() {
        connectionPool.shutdown();
        super.shutdown();
    }
}
