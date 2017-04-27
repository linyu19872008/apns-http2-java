package com.cdeer.apns.http2.core.service;


import com.cdeer.apns.http2.core.model.ApnsConfig;
import com.cdeer.apns.http2.core.model.NamedThreadFactory;
import com.cdeer.apns.http2.core.model.Payload;
import com.cdeer.apns.http2.core.model.PushNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractApnsService implements ApnsService {

    protected static final Logger log = LoggerFactory.getLogger(AbstractApnsService.class);

    /**
     * expire in minutes
     */
    private static final int EXPIRE = 15 * 60 * 1000;

    private static final AtomicInteger IDS = new AtomicInteger(0);

    protected ExecutorService executorService;

    public AbstractApnsService(ApnsConfig config) {
//        executorService = Executors.newFixedThreadPool(config.getPoolSize());
        executorService = Executors.newFixedThreadPool(config.getPoolSize(), new NamedThreadFactory(config.getName()));
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void sendNotification(String token, Payload payload) {
        PushNotification notification = new PushNotification();
        notification.setToken(token);
        notification.setPayload(payload);
        notification.setExpire(EXPIRE);
        notification.setId(IDS.incrementAndGet());
        sendNotification(notification);
    }

    @Override
    public void shutdown() {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(6, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("shutdown", e);
        }
    }
}
