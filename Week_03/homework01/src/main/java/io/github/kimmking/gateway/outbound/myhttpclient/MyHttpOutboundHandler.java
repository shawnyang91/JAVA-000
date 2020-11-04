package io.github.kimmking.gateway.outbound.myhttpclient;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @description:
 * @author: yangshumin
 * @create: 2020-11-04 20:25
 */
public class MyHttpOutboundHandler {

    /**
     * 编码格式。发送编码格式统一用UTF-8
     */
    private static final String ENCODING = "UTF-8";

    /**
     * 设置连接超时时间，单位毫秒。
     */
    private static final int CONNECT_TIMEOUT = 6000;

    /**
     * 请求获取数据的超时时间(即响应时间)，单位毫秒。
     */
    private static final int SOCKET_TIMEOUT = 6000;


    private String backendUrl;

    public MyHttpOutboundHandler(String backendUrl) {
        this.backendUrl = backendUrl.endsWith("/") ? backendUrl.substring(0, backendUrl.length() - 1) : backendUrl;
    }


    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) throws Exception {
        final String url = this.backendUrl + fullRequest.uri();
        doGet(fullRequest, ctx, url);
    }


    /**
     * 发送get请求
     *
     * @param fullRequest
     * @param ctx
     * @param url         请求地址
     * @return
     * @throws Exception
     */
    public void doGet(FullHttpRequest fullRequest, ChannelHandlerContext ctx, String url) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        httpGet.setConfig(requestConfig);

        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
            handleResponse(fullRequest, ctx, httpResponse);
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
    }


    private void handleResponse(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, final HttpResponse endpointResponse) throws Exception {
        FullHttpResponse response = null;
        try {
            byte[] body = EntityUtils.toByteArray(endpointResponse.getEntity());

            // 添加响应内容
            byte[] customContext = "\nhello gateway!".getBytes(ENCODING);
            byte[] bytes = new byte[body.length + customContext.length];
            System.arraycopy(body, 0, bytes, 0, body.length);
            System.arraycopy(customContext, 0, bytes, body.length, customContext.length);

            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(bytes));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", bytes.length);

            // 添加 header
            response.headers().set("time", "week03");

        } catch (Exception e) {
            e.printStackTrace();
            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
            exceptionCaught(ctx, e);
        } finally {
            if (fullRequest != null) {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.write(response);
                }
            }
            ctx.flush();
        }

    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
