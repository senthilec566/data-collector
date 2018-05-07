package com.perfspeed.collector;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.perfspeed.collector.utils.FileUtils;

public final class SslServerContext {

	private static final Logger LOG = LoggerFactory.getLogger(SslServerContext.class);
	
	private boolean useClientMode;
	private boolean wantClientAuth;
	private boolean needClientAuth;

	private String keystoreType;
	private String sslProtocol;
	private String sslKeyManagerFactoryAlgorithm;

	private String serverKeystorePath;
	private String trustStorePath;
	
	private String serverKeystorePassword;
	
	private SSLContext serverContext;
	
	public static class Builder{
		private boolean useClientMode;
		private boolean wantClientAuth;
		private boolean needClientAuth;

		private String keyStoreType;
		private String sslProtocol;
		private String sslKeyManagerFactoryAlgorithm;

		private String serverKeystorePath;
		private String trustStorePath;
		
		private String serverKeystorePassword;
		
		public Builder(String serverKeystorePath, String trustStorePath, String serverKeystorePassword) {
			this.serverKeystorePath = serverKeystorePath;
			this.trustStorePath = trustStorePath;
			this.serverKeystorePassword = serverKeystorePassword;
		}
		
		public Builder setUseClientMode(boolean useClientMode) {
			this.useClientMode = useClientMode;
			return this;
		}

		public Builder setWantClientAuth(boolean wantClientAuth) {
			this.wantClientAuth = wantClientAuth;
			return this;
		}

		public Builder setNeedClientAuth(boolean needClientAuth) {
			this.needClientAuth = needClientAuth;
			return this;
		}

		public Builder setKeyStoreType(String keyStoreType) {
			this.keyStoreType = keyStoreType;
			return this;
		}

		public Builder setSslProtocol(String sslProtocol) {
			this.sslProtocol = sslProtocol;
			return this;
		}

		public Builder setSslKeyManagerFactoryAlgorithm(String sslKeyManagerFactoryAlgorithm) {
			this.sslKeyManagerFactoryAlgorithm = sslKeyManagerFactoryAlgorithm;
			return this;
		}

		public SslServerContext build() {
			SslServerContext ctx =  new SslServerContext(useClientMode, 
					wantClientAuth,
					needClientAuth, 
					keyStoreType, 
					sslProtocol, 
					sslKeyManagerFactoryAlgorithm, 
					serverKeystorePath,
					trustStorePath,
					serverKeystorePassword);
			ctx.init();
			return ctx;
		}
	}

	private SslServerContext(boolean useClientMode, 
			boolean wantClientAuth, 
			boolean needClientAuth,
			String keyStoreType,
			String sslProtocol,
			String sslKeyManagerFactoryAlgo,
			String serverKeystorePath,
			String trustStorePath,
			String serverKeystorePassword) {

		this.useClientMode = useClientMode;
		this.wantClientAuth = wantClientAuth;
		this.needClientAuth = needClientAuth;
		this.keystoreType = keyStoreType;
		this.sslProtocol = sslProtocol;
		this.sslKeyManagerFactoryAlgorithm = sslKeyManagerFactoryAlgo;
		this.serverKeystorePath = serverKeystorePath;
		this.trustStorePath = trustStorePath;
		this.serverKeystorePassword = serverKeystorePassword;
	}
	
	private void init() {
		keystoreType = !Strings.isNullOrEmpty(keystoreType) ? keystoreType : "JKS";
		
		sslKeyManagerFactoryAlgorithm = !Strings.isNullOrEmpty(sslKeyManagerFactoryAlgorithm) ?
				sslKeyManagerFactoryAlgorithm : KeyManagerFactory.getDefaultAlgorithm();
		
		sslProtocol = !Strings.isNullOrEmpty(sslProtocol) ? sslProtocol : "TLS";
		
	}
	
	private void loadServerContext() throws Exception{
		final KeyStore ks = KeyStore.getInstance(keystoreType);
		try(final InputStream fis = FileUtils.getInputStream(serverKeystorePath)){
			ks.load(fis, serverKeystorePassword.toCharArray());
		}
		
		final KeyManagerFactory kmf = KeyManagerFactory.getInstance(sslKeyManagerFactoryAlgorithm);
		kmf.init(ks, serverKeystorePassword.toCharArray());

		final KeyStore ts = KeyStore.getInstance(keystoreType);
		try(final InputStream fis = FileUtils.getInputStream(trustStorePath)){
			ts.load(fis, serverKeystorePassword.toCharArray());
		}
		
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ts);
		
		SSLContext sslContext = SSLContext.getInstance(sslProtocol);
		sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
		this.serverContext = sslContext;

	}

	public SSLEngine createEngine() {
		SSLEngine sslEngine = null;
		try {
			loadServerContext();
			sslEngine = serverContext.createSSLEngine();
			sslEngine.setUseClientMode(useClientMode);
			sslEngine.setWantClientAuth(wantClientAuth);
			sslEngine.setNeedClientAuth(needClientAuth);
		}catch (Exception ex) {
			LOG.error(" Failed to Create SSL Engine", ex.getMessage());
			System.exit(1);
		}
		return sslEngine;
	}
}
