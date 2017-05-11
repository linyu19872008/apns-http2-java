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
                    boolean result = connection.sendNotification(notification);
                    if (result == false) {
                        log.debug("发送通知失败");
                    }
                }
            } catch (Exception e) {
                log.error("sendNotification", e);
            } finally {
                connectionPool.release(connection);
            }
        });
    }

    @Override
    public boolean sendNotificationSynch(PushNotification notification) {
        NettyApnsConnection connection = null;
        try {
            connection = connectionPool.acquire();
            if (connection != null) {
                boolean result = connection.sendNotification(notification);
                return result;
            }
        } catch (Exception e) {
            log.error("sendNotification", e);
        } finally {
            connectionPool.release(connection);
        }
        return false;
    }


    @Override
    public void shutdown() {
        connectionPool.shutdown();
        super.shutdown();
    }
}
