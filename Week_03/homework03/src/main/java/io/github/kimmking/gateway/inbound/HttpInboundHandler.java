package io.github.kimmking.gateway.inbound;

import io.github.kimmking.gateway.filter.HttpRequestFilter;
import io.github.kimmking.gateway.filter.HttpRequestFilterImpl;
import io.github.kimmking.gateway.outbound.myhttpclient.MyHttpOutboundHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;

public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private final String proxyServer;

    /**
     * 自定义 Handler
     */
    private MyHttpOutboundHandler handler;
    private HttpRequestFilter filter;

    public HttpInboundHandler(String proxyServer, HttpRequestFilterImpl filter) {
        this.proxyServer = proxyServer;
        this.filter = filter;
        this.handler = new MyHttpOutboundHandler(this.proxyServer);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest fullRequest = (FullHttpRequest) msg;
            if (filter != null) {
                filter.filter(fullRequest, ctx);
            }
            handler.handle(fullRequest, ctx);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
