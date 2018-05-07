package com.perfspeed.collector.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.perfspeed.collector.NettyServerConfig;
import com.perfspeed.collector.SslServerContext;
import com.perfspeed.collector.httpclient.AsyncExecutionHandler;

/**
 * Utility API for Data Collector Process 
 * @author skalaise
 *
 */
public final class CollectorUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(AsyncExecutionHandler.class);
	
	public final static Properties applicationConfig = new Properties();
	
	/**
	 * Parse user arguments 
	 * @param args
	 * @return
	 */
	public static CommandLine parseArgs(String[] args) {
		LOG.info("Parsing CommandLine argument ...");
		final Options options = new Options();
		final Option appConf = new Option("conf", "application conf", true, "config path for application pro");
		appConf.setRequired(true);
		options.addOption(appConf);
		final CommandLineParser parser = new DefaultParser();
		final HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (Exception ex) {
			formatter.printHelp("DataReceiver", options);
			LOG.error("Failed to Parse Command Line Args ", ex);
			System.exit(1);
		}
		return cmd;
	}
	
	/**
	 * Load Application Config File to Properties File
	 * @param appConfPath
	 * @throws IOException
	 */
	public static void loadAppConfig(final String appConfPath ) throws IOException{
		try(final InputStream is = FileUtils.getInputStream(appConfPath)){
			applicationConfig.load(is);
		}
	}
	
	/**
	 * Construct Druid Tranquility Server EndPoint
	 * @return http post endpoint for streaming data to druid
	 */
	public static String getDruidEndPoint(){
		final StringBuffer buff = new StringBuffer();
		buff.append(applicationConfig.getProperty("tranquility.server.protocol","http")).append("://");
		buff.append(applicationConfig.getProperty("tranquility.server.hostname")).append(":");
		buff.append(applicationConfig.getProperty("tranquility.server.port", "8080"));
		buff.append(applicationConfig.getProperty("tranquility.server.path"));
		buff.append(applicationConfig.getProperty("tranquility.server.datasource"));
		return buff.toString();
	}
	
	/**
	 * Find GeoLite DB Path
	 * @return
	 */
	public static String getGeoDBPath(){
		final StringBuffer buff = new StringBuffer();
		buff.append(applicationConfig.getProperty("geolite.db.path"));
		return buff.toString();
	}
	
	/**
	 * Prepare Netty Server Config
	 * @return
	 * @throws Exception
	 */
	public static NettyServerConfig prepareNettyServerConf() throws Exception{
		SslServerContext sslServerContext = null;
		final NettyServerConfig conf = new NettyServerConfig();
		String transportMode = applicationConfig.getProperty("transport_mode");//either http , https
		int port = 8080;
		String portStr = applicationConfig.getProperty("transport_port");
		if(!Strings.isNullOrEmpty(portStr)) {
			port = Integer.valueOf(portStr);
		}
		conf.setSslEnabled(false);
		if(!Strings.isNullOrEmpty(transportMode) && !transportMode.equalsIgnoreCase("http")) {
			conf.setSslEnabled(true);
			LOG.info("Configuring for https connections");
			final String keyStorePath = applicationConfig.getProperty("key_store_path");
			final String trustStorePath = applicationConfig.getProperty("trust_store_path");
			final String keyStorePwd = applicationConfig.getProperty("key_store_password");
			if(Strings.isNullOrEmpty(keyStorePwd) || Strings.isNullOrEmpty(keyStorePath) || !(new File(keyStorePath).isFile())) 
				throw new Exception("Either KeystorePath or KeyStorePassword is null");

			final boolean useClientMode = Boolean.valueOf(applicationConfig.getProperty("use_client_mode"));
			final boolean wantClientAuth = Boolean.valueOf(applicationConfig.getProperty("want_client_auth"));
			final boolean needClientAuth = Boolean.valueOf(applicationConfig.getProperty("need_client_auth"));
			final String keyStoreType = applicationConfig.getProperty("key_store_type");
			final String sslProtocol = applicationConfig.getProperty("ssl_protocol");
			final String sslKeyManagerFactoryAlgorithm = applicationConfig.getProperty("sslkey_factory_algorithm");
			final SslServerContext.Builder builder = new SslServerContext.Builder(keyStorePath,trustStorePath,keyStorePwd);
			builder.setKeyStoreType(keyStoreType)
			.setSslProtocol(sslProtocol)
			.setSslKeyManagerFactoryAlgorithm(sslKeyManagerFactoryAlgorithm)
			.setUseClientMode(useClientMode)
			.setWantClientAuth(wantClientAuth)
			.setNeedClientAuth(needClientAuth);
			sslServerContext = builder.build();
			port = 8443;
		}
		LOG.info("Setting the port as  : {} ", port);
		conf.setBossGroupThreads(1);  
		conf.setWorkerGroupThreads(0); // expose this to user 
		conf.setListenPort(port);
		conf.setHostName();
		conf.setSslServerContext(sslServerContext);
		return conf;
	}
}
