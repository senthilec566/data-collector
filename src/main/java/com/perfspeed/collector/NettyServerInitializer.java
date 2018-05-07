package com.perfspeed.collector;

import com.perfspeed.collector.handler.PostRequestHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
	
	private final SslServerContext sslCtx;

	public NettyServerInitializer(SslServerContext sslCtx) {
		this.sslCtx = sslCtx;
	}

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        if (this.sslCtx != null) {
        		p.addFirst(new SslHandler(this.sslCtx.createEngine()));
        }
        p.addLast(new HttpServerCodec());
        p.addLast("aggregator", new HttpObjectAggregator(100000)); // 100 KB max size 
        p.addLast(new PostRequestHandler());
    }

}
