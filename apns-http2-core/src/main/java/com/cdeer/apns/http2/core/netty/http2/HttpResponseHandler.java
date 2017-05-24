package com.cdeer.apns.http2.core.netty.http2;

import com.cdeer.apns.http2.core.error.ErrorDispatcher;
import com.cdeer.apns.http2.core.error.ErrorModel;
import com.cdeer.apns.http2.core.model.PushNotification;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * http2响应处理器
 */
public class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    public static final int CODE_SUCCESS = 0;

    public static final int CODE_WRITE_TIMEOUT = 1;

    public static final int CODE_WRITE_FAILED = 2;

    public static final int CODE_READ_TIMEOUT = 3;

    public static final int CODE_READ_FAILED = 4;

    private static final Logger log = LoggerFactory.getLogger(HttpResponseHandler.class);
    private String name;

    private ConcurrentHashMap<Integer, Entry<ChannelFuture, ChannelPromise>> streamIdPromiseMap;

    public ConcurrentHashMap<Integer, PushNotification> notificationMap;


    HttpResponseHandler(String name) {
        this.name = name;
        streamIdPromiseMap = new ConcurrentHashMap<>();
        notificationMap = new ConcurrentHashMap<>();
    }

    /**
     * 移除通知
     *
     * @param streamId
     * @return
     */
    public PushNotification removeNotification(int streamId) {
        return notificationMap.remove(streamId);
    }

    /**
     * Create an association between an anticipated response stream id and a {@link
     * io.netty.channel.ChannelPromise}
     *
     * @param streamId     The stream for which a response is expected
     * @param notification 发送的通知
     * @param writeFuture  A future that represent the request write operation
     * @param promise      The promise object that will be used to wait/notify events
     * @return The previous object associated with {@code streamId}
     * @see HttpResponseHandler#awaitResponses(long, TimeUnit)
     */
    public Entry<ChannelFuture, ChannelPromise> put(int streamId, PushNotification notification, ChannelFuture writeFuture, ChannelPromise promise) {
        // 将通知添加到集合
        notificationMap.put(streamId, notification);
        Entry<ChannelFuture, ChannelPromise> mapFuture = streamIdPromiseMap.put(streamId, new SimpleEntry<>(writeFuture, promise));
        dumpStreamIdPromiseMap("put");
        return mapFuture;
    }

    private void dumpStreamIdPromiseMap(String tag) {
//        if (!log.isDebugEnabled()) {
//            return;
//        }

//        StringBuilder builder = new StringBuilder();
//        builder.append(tag).append(" ").append("streamIdMap: ");
//        for (Integer streamId : streamIdPromiseMap.keySet()) {
//            builder.append(streamId).append(" ");
//        }
//        log.info(builder.toString());
    }

    /**
     * Wait (sequentially) for a time duration for each anticipated response
     *
     * @param timeout Value of time to wait for each response
     * @param unit    Units associated with {@code timeout}
     */
    public Map<Integer, Integer> awaitResponses(long timeout, TimeUnit unit) {
        dumpStreamIdPromiseMap("awaitResponses");
        HashMap<Integer, Integer> responses = new HashMap<>();
        Iterator<Entry<Integer, Entry<ChannelFuture, ChannelPromise>>> itr = streamIdPromiseMap.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<Integer, Entry<ChannelFuture, ChannelPromise>> entry = itr.next();
            ChannelFuture writeFuture = entry.getValue().getKey();
            if (!writeFuture.awaitUninterruptibly(timeout, unit)) {
                responses.put(entry.getKey(), CODE_WRITE_TIMEOUT);
                log.info("write id " + entry.getKey() + " timeout");
                continue;
            }

            if (!writeFuture.isSuccess()) {
                responses.put(entry.getKey(), CODE_WRITE_FAILED);
                itr.remove();
                log.info("write id " + entry.getKey() + " failed");
                continue;
            }

            ChannelPromise promise = entry.getValue().getValue();

            if (!promise.awaitUninterruptibly(timeout, unit)) {
                log.info("read id " + entry.getKey() + " timeout");
                responses.put(entry.getKey(), CODE_READ_TIMEOUT);
                continue;
            }

            if (!promise.isSuccess()) {
                responses.put(entry.getKey(), CODE_READ_FAILED);
                itr.remove();
                log.info("read id " + entry.getKey() + " failed");
                continue;
            }

            responses.put(entry.getKey(), 0);

//            log.info("stream id: " + entry.getKey() + " received");
            itr.remove();
        }
        return responses;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        dumpStreamIdPromiseMap("channelRead0");
        Integer streamId = msg.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());

        int code = msg.status().code();
        PushNotification notification = notificationMap.remove(streamId);
        if (code != 200) {
            log.debug("response[" + code + "],[streamId:" + streamId + "][token:" + notification + "]");
            ErrorModel errorModel = new ErrorModel();
            errorModel.setAppName(name);
            errorModel.setCode(code);
            errorModel.setNotification(notification);
            ErrorDispatcher.getInstance().dispatch(errorModel);
        }

        if (streamId == null) {
            log.error("HttpResponseHandler unexpected message received: " + msg);
            return;
        }

        Entry<ChannelFuture, ChannelPromise> entry = streamIdPromiseMap.get(streamId);
        if (entry == null) {
            log.error("Message received for unknown stream id " + streamId);
        } else {
            // Do stuff with the message (for now just print it)
            ByteBuf content = msg.content();
            if (content.isReadable()) {
                int contentLength = content.readableBytes();
                byte[] arr = new byte[contentLength];
                content.readBytes(arr);
                String result = new String(arr, 0, contentLength, CharsetUtil.UTF_8);
//                PushNotification notification = notificationMap.remove(streamId);
//                log.info("response[" + msg.status().code() + "],[streamId:" + streamId + "][token:" + notification.getToken() + "]:" + result);
//                if (msg.status().code() != 200) {
//                    ctx.channel().close();
//                }
            }

            entry.getValue().setSuccess();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.error("TCP连接断开" + ctx.channel());
        super.channelInactive(ctx);
    }
}
