package com.cdeer.apns.http2.core.netty.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Flags;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http2日志处理器
 */
public class SimpleHttp2FrameLogger extends Http2FrameLogger {

    private static final Logger log = LoggerFactory.getLogger(SimpleHttp2FrameLogger.class);

    private String name;

    SimpleHttp2FrameLogger(String name) {
        super(LogLevel.DEBUG, name);
        this.name = name;
    }

    @Override
    public void logData(Direction direction, ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endStream) {
        log(direction, "DATA: streamId=%d, endStream=%b", streamId, endStream);
    }

    @Override
    public void logHeaders(Direction direction, ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endStream) {
        log(direction, "HEADERS: streamId=%d, endStream=%b", streamId, endStream);
    }

    @Override
    public void logHeaders(Direction direction, ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endStream) {
        log(direction, "HEADERS: streamId=%d endStream=%b", streamId, endStream);
    }

    @Override
    public void logPriority(Direction direction, ChannelHandlerContext ctx, int streamId, int streamDependency, short weight, boolean exclusive) {
        log(direction, "PRIORITY: streamId=%d, streamDependency=%d, weight=%d, exclusive=%b",
                streamId, streamDependency, weight, exclusive);
    }

    @Override
    public void logRstStream(Direction direction, ChannelHandlerContext ctx, int streamId, long errorCode) {
        log(direction, "RST_STREAM: streamId=%d, errorCode=%d", streamId, errorCode);
    }

    @Override
    public void logSettingsAck(Direction direction, ChannelHandlerContext ctx) {
        log(direction, "SETTINGS: ack=true");
    }

    @Override
    public void logSettings(Direction direction, ChannelHandlerContext ctx, Http2Settings settings) {
        log(direction, "SETTINGS: ack=false, settings=%s", settings);
    }

    @Override
    public void logPing(Direction direction, ChannelHandlerContext ctx, ByteBuf data) {
        log(direction, "PING: ack=false");
    }

    @Override
    public void logPingAck(Direction direction, ChannelHandlerContext ctx, ByteBuf data) {
        log(direction, "PING: ack=true");
    }

    @Override
    public void logPushPromise(Direction direction, ChannelHandlerContext ctx, int streamId, int promisedStreamId, Http2Headers headers, int padding) {
        log(direction, "PUSH_PROMISE: streamId=%d, promisedStreamId=%d, headers=%s, padding=%d", streamId, promisedStreamId, headers, padding);
    }

    @Override
    public void logGoAway(Direction direction, ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData) {
        log(direction, "GO_AWAY: lastStreamId=%d, errorCode=%d", lastStreamId, errorCode);
    }

    @Override
    public void logWindowsUpdate(Direction direction, ChannelHandlerContext ctx, int streamId, int windowSizeIncrement) {
        log(direction, "WINDOW_UPDATE: streamId=%d, windowSizeIncrement=%d", streamId, windowSizeIncrement);
    }

    @Override
    public void logUnknownFrame(Direction direction, ChannelHandlerContext ctx, byte frameType, int streamId, Http2Flags flags, ByteBuf data) {
        log(direction, "UNKNOWN: frameType=%d, streamId=%d, flags=%d", frameType & 0xFF, streamId, flags.value());
    }

    private void log(Direction direction, String format, Object... args) {
        log.debug(name + " " + direction.name() + " " + String.format(format, args));
    }
}
