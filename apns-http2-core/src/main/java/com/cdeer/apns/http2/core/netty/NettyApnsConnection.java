package com.cdeer.apns.http2.core.netty;

import com.cdeer.apns.http2.core.model.PushNotification;
import com.cdeer.apns.http2.core.netty.http2.Http2ClientInitializer;
import com.cdeer.apns.http2.core.netty.http2.Http2SettingsHandler;
import com.cdeer.apns.http2.core.netty.http2.HttpResponseHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.cdeer.apns.http2.core.netty.http2.HttpResponseHandler.*;

/**
 * 使用Netty建立的TCP连接
 */
public class NettyApnsConnection {

    private static final Logger log = LoggerFactory.getLogger(NettyApnsConnection.class);

    private static final int INITIAL_STREAM_ID = 3;

    private int retryTimes;

    private KeyManagerFactory keyManagerFactory;

    private Channel channel;

    private AtomicInteger streamId = new AtomicInteger(INITIAL_STREAM_ID);

    private Http2ClientInitializer http2ClientInitializer;

    public NettyApnsConnectionPool connectionPool;

    private String host;

    private String name;

    private int port;

    private int timeout;

    private String topic;

    public NettyApnsConnection(String name, String host, int port, int retryTimes, int timeout,
                               String topic, KeyManagerFactory keyManagerFactory) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.retryTimes = retryTimes;
        this.timeout = timeout;
        this.topic = topic;
        this.keyManagerFactory = keyManagerFactory;
    }

    public void setConnectionPool(NettyApnsConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

//    public void sendNotification(PushNotification notification) {
//        if (connectionPool.isShutdown()) {
//            return;
//        }
////        log.info("send payload " + notification.getPayload().toString() + " token " + notification.getToken());
//        int retryCount = 0;
//        int streamId = -1;
//        boolean success = false;
//        FullHttpRequest request = null;
//        while (retryCount < retryTimes && !connectionPool.isShutdown()) {
//            if (channel == null || !channel.isActive()) {
//                try {
//                    initializeNettyClient();
//                } catch (Exception e) {
//                    log.error("initializeNettyClient", e);
//                    http2ClientInitializer = null;
//                    retryCount++;
//                    continue;
//                }
//            }
//
//            HttpResponseHandler responseHandler = http2ClientInitializer.responseHandler();
//            if (request == null) {
//                request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
//                        HttpMethod.POST, "https://" + host + "/3/device/" + notification.getToken(),
//                        Unpooled.copiedBuffer(notification.getPayload().toString().getBytes()));
//                request.headers().add("apns-topic", topic);
//
//                // streamId过大,重新初始化
//                if (this.streamId.get() > 200000000) {
//                    this.streamId.set(INITIAL_STREAM_ID);
//                }
//                streamId = this.streamId.getAndAdd(2);
////                log.info("发送通知,[streamId:" + streamId + "][token:" + notification.getToken() + "]");
//
//                responseHandler.put(streamId, notification, channel.writeAndFlush(request), channel.newPromise());
//            }
//
//            Map<Integer, Integer> responses = responseHandler.awaitResponses(timeout, TimeUnit.MILLISECONDS);
//            int code = responses.get(streamId);
//            if (code == CODE_SUCCESS) {
////                log.info("send success token " + notification.getToken());
//                responseHandler.removeNotification(streamId);
//                success = true;
//                break;
//            } else if (code == CODE_READ_TIMEOUT || code == CODE_WRITE_TIMEOUT) {
//                retryCount++;
//                responseHandler.removeNotification(streamId);
//            } else if (code == CODE_READ_FAILED || code == CODE_WRITE_FAILED) {
//                responseHandler.removeNotification(streamId);
//                request = null;
//                retryCount++;
//                try {
//                    channel.close().sync();
//                } catch (InterruptedException e) {
//                    log.error("close channel", e);
//                }
//            }
//        }
//
//        if (!success) {
//            log.error("send failed token " + notification.getToken() + ", size:" + http2ClientInitializer.responseHandler().notificationMap.size());
//        }
//    }


    public boolean sendNotification(PushNotification notification) {
        if (connectionPool.isShutdown()) {
            log.error("线程池已经死掉");
            return false;
        }

        if (channel == null || !channel.isActive()) {
            try {
                initializeNettyClient();
            } catch (Exception e) {
                log.error("initializeNettyClient", e);
                http2ClientInitializer = null;
                return false;
            }
        }
//        log.info("send payload " + notification.getPayload().toString() + " token " + notification.getToken());

        FullHttpRequest request = null;
        HttpResponseHandler responseHandler = http2ClientInitializer.responseHandler();
        if (request == null) {
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                    HttpMethod.POST, "https://" + host + "/3/device/" + notification.getToken(),
                    Unpooled.copiedBuffer(notification.getPayload().toString().getBytes()));
            request.headers().add("apns-topic", topic);

            // streamId过大,重新初始化
            if (this.streamId.get() > 200000000) {
                this.streamId.set(INITIAL_STREAM_ID);
            }
            int streamId = this.streamId.getAndAdd(2);

            ChannelFuture channelFuture = channel.writeAndFlush(request);
            responseHandler.put(streamId, notification, channelFuture, channel.newPromise());
            channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
//                    log.info("channel写执行完成:" + future);
                }
            });
            channelFuture.awaitUninterruptibly();
            boolean success = channelFuture.isSuccess();
//            log.info("channel写执行结束===============:"+channelFuture);
            if(success){
                return true;
            }else{
                return false;
            }
        }
        return false;

    }


    private void initializeNettyClient() throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Http2ClientInitializer http2ClientInitializer = new Http2ClientInitializer(name,
                createSslContext(), Integer.MAX_VALUE);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .remoteAddress(host, port)
                .handler(http2ClientInitializer);

        log.info("connecting to " + host);
        channel = bootstrap.connect().syncUninterruptibly().channel();
        log.info("connected");

        Http2SettingsHandler http2SettingsHandler = http2ClientInitializer.settingsHandler();
        log.info("await setting");
        http2SettingsHandler.awaitSettings(30, TimeUnit.SECONDS);
        log.info("setting success");

        streamId.set(INITIAL_STREAM_ID);
        this.http2ClientInitializer = http2ClientInitializer;
    }


    private SslContext createSslContext() throws SSLException {
        SslProvider provider = SslProvider.JDK;
        return SslContextBuilder.forClient()
                .sslProvider(provider)
                /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                 * Please refer to the HTTP/2 specification for cipher requirements. */
                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .keyManager(keyManagerFactory)
                .applicationProtocolConfig(ApplicationProtocolConfig.DISABLED)
                .build();
    }
}
