package com.perfspeed.collector;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.perfspeed.collector.utils.CollectorUtils;


/**
 * Data Collector this accept only POST events
 * Collects data from different systems and persist in Druid
 * <example>
 * WebBrowser
 * POST --data {event} https://datacollector/collect/logs/
 * This server responsible for 
 * 1) Accept the incoming request from trusted source  
 * 2) Valdiate if any rules
 * 3) Write it to Druid or Kafka 
 * </example>
 * @author skalaise
 *
 */
public class DataCollector {

	private static final Logger LOG = LoggerFactory.getLogger(DataCollector.class);
	/**
	 * Entry for datacollector 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		LOG.info("Starting Data Collector Service ...");
		LOG.info("Arguments : " + Arrays.toString(args));
		// Parse Commands
		final CommandLine cmd = CollectorUtils.parseArgs(args);
		CollectorUtils.loadAppConfig(cmd.getOptionValue("conf"));
		final NettyServerConfig conf = CollectorUtils.prepareNettyServerConf();
		final NettyServer server = new NettyServer();
		server.start(conf);
		LOG.info("Server started and ready to accept events ...");
	}
}
