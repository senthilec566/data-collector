package com.perfspeed.collector.httpclient;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;

public class HttpClientBuilder {
	
	public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 1200 * 1000 ; // 30 seconds as timeout 
    public static final int DEFAULT_SOCKET_TIMEOUT_MILLIS = 60 * 1000; // socket timeout as 10 seconds 
    public static final int DEFAULT_MAX_RETRY_TIMEOUT_MILLIS = DEFAULT_SOCKET_TIMEOUT_MILLIS;
    public static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT_MILLIS = 30 * 1000 ; // connection request timeout 5 seconds 
    public static final int DEFAULT_MAX_CONN_PER_ROUTE = 10;
    public static final int DEFAULT_MAX_CONN_TOTAL = 50; // max connection to 50 
    public static final int DEFAULT_MAX_HEADER_COUNTER = 200; 
    public static final int DEFAULT_MAX_LINE_LENGTH = 2000;
    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
    
    public static final IOReactorConfig.Builder ioReactorConfigBuilder = IOReactorConfig.custom()
            .setIoThreadCount(Runtime.getRuntime().availableProcessors())
            .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS)
            .setSoTimeout(DEFAULT_SOCKET_TIMEOUT_MILLIS)
            .setTcpNoDelay(true)
            .setSoKeepAlive(true)
            .setSoReuseAddress(true);
    
    public static final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
            .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS)
            .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT_MILLIS)
            .setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT_MILLIS);
	
    public static final MessageConstraints messageConstraints = MessageConstraints.custom()
            .setMaxHeaderCount(DEFAULT_MAX_HEADER_COUNTER)
            .setMaxLineLength(DEFAULT_MAX_LINE_LENGTH)
            .build();
    public static final ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setBufferSize(DEFAULT_BUFFER_SIZE)
            .setMessageConstraints(messageConstraints)
            .setFragmentSizeHint(DEFAULT_BUFFER_SIZE)
            .build();
    
    public static final HostnameVerifier hostNameVerifier = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true; // return try by default
		}
	};
	
	public static CloseableHttpAsyncClient buildAsyncHttpClient()  {
        ConnectingIOReactor ioreactor;
        CloseableHttpAsyncClient httpClient;
		try {
			ioreactor = new DefaultConnectingIOReactor(ioReactorConfigBuilder.build());
			final PoolingNHttpClientConnectionManager mgr = new PoolingNHttpClientConnectionManager(ioreactor);
	        mgr.setDefaultConnectionConfig(connectionConfig);
	        mgr.setMaxTotal(DEFAULT_MAX_CONN_TOTAL);
	        mgr.setDefaultMaxPerRoute(DEFAULT_MAX_CONN_PER_ROUTE);
			final HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClientBuilder.create()
														.setDefaultRequestConfig(requestConfigBuilder.build())
														.setMaxConnPerRoute(DEFAULT_MAX_CONN_PER_ROUTE)
														.setMaxConnTotal(DEFAULT_MAX_CONN_TOTAL)
														.setConnectionManager(mgr)
														.setSSLContext(getSSLContext())
														.setSSLHostnameVerifier(hostNameVerifier)
														.useSystemProperties();
			httpClient = httpClientBuilder.build();
			httpClient.start();
			return httpClient;
		} catch (IOReactorException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	 
	public static CloseableHttpAsyncClient buildAsyncHttpClient(final String userName , final String passWord ) throws Exception{
		ioReactorConfigBuilder.setConnectTimeout( 2 * DEFAULT_CONNECT_TIMEOUT_MILLIS)
							  .setConnectTimeout(2 * DEFAULT_CONNECT_TIMEOUT_MILLIS)
							  .setSoTimeout(2 * DEFAULT_SOCKET_TIMEOUT_MILLIS); // ensure to wait for long time since this requires authentication too
		final ConnectingIOReactor ioreactor  = new DefaultConnectingIOReactor(ioReactorConfigBuilder.build());
        final PoolingNHttpClientConnectionManager mgr = new PoolingNHttpClientConnectionManager(ioreactor);
        mgr.setDefaultConnectionConfig(connectionConfig);
        mgr.setMaxTotal(DEFAULT_MAX_CONN_TOTAL);
        mgr.setDefaultMaxPerRoute(DEFAULT_MAX_CONN_PER_ROUTE);
        
        requestConfigBuilder.setConnectTimeout( 2 * DEFAULT_CONNECT_TIMEOUT_MILLIS)
        					.setSocketTimeout( 2 * DEFAULT_SOCKET_TIMEOUT_MILLIS)
        					.setConnectionRequestTimeout( 4 * DEFAULT_CONNECT_TIMEOUT_MILLIS ) // to assure data loss use -1 but revisit this 
        					.setAuthenticationEnabled(true); // ensure to enable authentication
		final CredentialsProvider credsProvider = new BasicCredentialsProvider();
		final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, passWord);
		credsProvider.setCredentials(AuthScope.ANY,credentials);
		final HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClientBuilder.create()
													.setDefaultRequestConfig(requestConfigBuilder.build())
													.setMaxConnPerRoute(DEFAULT_MAX_CONN_PER_ROUTE)
													.setMaxConnTotal(DEFAULT_MAX_CONN_TOTAL)
													.setConnectionManager(mgr)
													.setSSLContext(getSSLContext())
													.setSSLHostnameVerifier(hostNameVerifier)
													.setDefaultCredentialsProvider(credsProvider)
													
													.useSystemProperties();
		CloseableHttpAsyncClient httpClient = httpClientBuilder.build();
		httpClient.start();
		return httpClient;
	}
	
	public static SSLContext getSSLContext() throws KeyManagementException, NoSuchAlgorithmException{
		final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
			}
			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
			}
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}
		} };
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
		return sslContext;
	}
}