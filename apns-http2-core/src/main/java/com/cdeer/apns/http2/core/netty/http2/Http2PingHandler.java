package com.cdeer.apns.http2.core.netty.http2;

import com.cdeer.apns.http2.core.model.PingMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;

/**
 * http2心跳处理器
 */
public class Http2PingHandler extends ChannelOutboundHandlerAdapter {

    private Http2ConnectionEncoder encoder;

    Http2PingHandler(Http2ConnectionEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof PingMessage)) {
            ctx.write(msg, promise);
            return;
        }

        encoder.writePing(ctx, false, Http2CodecUtil.emptyPingBuf(), promise);
        ctx.channel().flush();
    }
}
