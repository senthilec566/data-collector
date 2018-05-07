package com.perfspeed.collector.handler;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.perfspeed.collector.geo.GeoIPLocationService;
import com.perfspeed.collector.httpclient.AsyncExecutionHandler;
import com.perfspeed.collector.httpclient.HttpClientBuilder;
import com.perfspeed.collector.utils.HttpUtils;
import com.perfspeed.collector.utils.JsonParser;
import com.perfspeed.collector.utils.StringUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;

/**
 * Data Handler - Sharable 
 * Responsibilites 
 * 1) Receive Full HttpRequest
 * 2) Read Request Contents
 * 3) Write content to destination( Druid )
 * 4) Respond to the request 
 * 
 * @author skalaise
 *
 */
@Sharable
public class PostRequestHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(PostRequestHandler.class);
	private final CloseableHttpAsyncClient client = HttpClientBuilder.buildAsyncHttpClient();

	@Override
	public void channelReadComplete( final ChannelHandlerContext ctx ) {
		ctx.flush();
	}

	/**
	 * Read Channel API 
	 */
	@Override
	public void channelRead( final ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof FullHttpRequest) {
			final FullHttpRequest fReq = (FullHttpRequest) msg;
			if (HttpUtil.is100ContinueExpected(fReq)) {
				ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
			}
			final String data = readIncomingData(fReq);
			if ( data == null ) {
				writeBadReqResponse(ctx);
				return;
			}
			final Map<String,String> dataMap = StringUtils.parseMap(data);
			final String clientIPAddr = getClientIPAddr(ctx);
			dataMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
			if(Objects.nonNull(clientIPAddr) && !clientIPAddr.isEmpty() )
				GeoIPLocationService.addGeo(clientIPAddr, dataMap);
			final String finalJson = JsonParser.map2Json(dataMap);
			AsyncExecutionHandler.executeCall(client, HttpUtils.createPostReq(finalJson));
			LOG.info(finalJson);
			writeResponse(fReq, ctx, true);
		}
	}
	
	private String getClientIPAddr(final ChannelHandlerContext ctx ){
		try{
			final InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
			return socketAddress.getAddress().getHostAddress();
		}catch (Exception e) {
			LOG.error("Faile to fetch Client IP Address ",e);
		}
		return null;
	}

	/**
	 * Handles excpetion in channel 
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	/**
	 * Read incoming request POST body 
	 * @param fReq
	 * @return
	 */
	public String readIncomingData( final FullHttpRequest fReq ){
		String data = null;
		final ByteBuf buf = fReq.content();
		if( buf == null ){
			return null;
		}
		try{
			if (buf.isReadable()) {
				byte[] result = new byte[buf.readableBytes()];
    			buf.readBytes(result);
    		    data = new String(result);
			}
		}catch(Exception e ){
			LOG.error("Faile to readIncomingData ",e);
		}finally {
			ReferenceCountUtil.release(buf);
		}
		return data;
	}

	/**
	 * Respond to client either success or failure 
	 * @param fReq
	 * @param ctx
	 * @param success
	 */
	public void writeResponse( final FullHttpRequest fReq , final ChannelHandlerContext ctx , final boolean success ){
		boolean keepAlive = HttpUtil.isKeepAlive(fReq);
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK , Unpooled.wrappedBuffer("SUCCESS".getBytes()) );
		if( !success ) // maximum case would be success :-) 
			response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK , Unpooled.wrappedBuffer("FAILURE".getBytes()) );
		addResponseHeaders(response);
		if (!keepAlive) {
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		} else {
			response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			ctx.writeAndFlush(response);
		}
	}

	/**
	 * Initimate Client that bad request is received  
	 * @param ctx
	 */
	public void writeBadReqResponse(final ChannelHandlerContext ctx){
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST , Unpooled.wrappedBuffer("BADREQ".getBytes()) );
		addResponseHeaders(response);
		ctx.writeAndFlush(response, ctx.voidPromise());
	}

	/**
	 * Add Response headers 
	 * @param response
	 */
	public void addResponseHeaders(final FullHttpResponse response ){
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
	}
}
