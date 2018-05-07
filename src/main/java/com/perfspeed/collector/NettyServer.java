package com.perfspeed.collector;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Netty based Http(s) Server 
 * @author skalaise
 *
 */
public class NettyServer {

	private static final Logger LOG = LoggerFactory.getLogger(DataCollector.class);
    
	/**
	 * Create Netty Bootstrp and bind it to port 
	 * @param config
	 * @throws Exception
	 */
	public void start( NettyServerConfig config ) throws Exception {
		
		final SslServerContext sslCtx;
        if (config.isSslEnabled()) {
        		sslCtx = config.getSslServerContext();
        } else {
            sslCtx = null;
        }
        
		final EventLoopGroup bossGroup;
	    final EventLoopGroup workerGroup;
	    
	    if (Epoll.isAvailable()) {
	    	LOG.info("Event Loop: epoll");
            bossGroup = new EpollEventLoopGroup(config.getBossGroupThreads());
            workerGroup = new EpollEventLoopGroup();
        } else {
        	LOG.info("Event Loop: NIO");
            bossGroup = new NioEventLoopGroup(config.getBossGroupThreads());
            workerGroup = new NioEventLoopGroup();
        }
	    
	    try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.group(bossGroup, workerGroup)
             .option(ChannelOption.SO_REUSEADDR, true);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.childHandler(new NettyServerInitializer(sslCtx));
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            
            if (Epoll.isAvailable()) {
                bootstrap.channel(EpollServerSocketChannel.class);
            } else {
                bootstrap.channel(NioServerSocketChannel.class);
            }
            // Bind and start to accept incoming connections.
            ChannelFuture future = bootstrap.bind(config.getListenPort()).sync();
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
	}
}
