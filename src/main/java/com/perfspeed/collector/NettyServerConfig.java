package com.perfspeed.collector;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NettyServerConfig {
	
	private String hostName;
	private int listenPort; 
	private int bossGroupThreads;
	private int workerGroupThreads;
	private boolean sslEnabled;
	
	private SslServerContext sslServerContext;
	
	public SslServerContext getSslServerContext() {
		return sslServerContext;
	}

	public void setSslServerContext(SslServerContext sslServerContext) {
		this.sslServerContext = sslServerContext;
	}
	
	public String getHostName() {
		return hostName;
	}
	
	public void setHostName() {
		try {
			this.hostName = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public int getListenPort() {
		return listenPort;
	}
	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}
	public int getBossGroupThreads() {
		return bossGroupThreads;
	}
	public void setBossGroupThreads(int bossGroupThreads) {
		this.bossGroupThreads = bossGroupThreads;
	}
	public int getWorkerGroupThreads() {
		return workerGroupThreads;
	}
	public void setWorkerGroupThreads(int workerGroupThreads) {
		this.workerGroupThreads = workerGroupThreads;
	}
	public boolean isSslEnabled() {
		return sslEnabled;
	}
	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}
}
