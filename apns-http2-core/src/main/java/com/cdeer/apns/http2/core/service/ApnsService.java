package com.cdeer.apns.http2.core.service;


import com.cdeer.apns.http2.core.model.Payload;
import com.cdeer.apns.http2.core.model.PushNotification;

public interface ApnsService {

    void sendNotification(String token, Payload payload);

    void sendNotification(PushNotification notification);

    void shutdown();

}
