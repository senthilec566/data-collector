package com.perfspeed.collector.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {

	private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);
	private static final String DRUID_ENDPOINT = CollectorUtils.getDruidEndPoint();
	
	public static String getIncomingRequestEntity(final HttpEntity entity){
		try {
			return EntityUtils.toString(entity);
		} catch (ParseException | IOException e) {
			LOG.error("Failed to Parse Http Post Request  ", e);
		}
		return null;
	}
	
	
	public static HttpPost createPostReq(final String data) throws URISyntaxException{
		final HttpPost httpPost = new HttpPost();
		LOG.info("Posting to : {} ", DRUID_ENDPOINT);
		httpPost.setURI(new URI(DRUID_ENDPOINT));
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setEntity(new NByteArrayEntity(data.getBytes()));
		return httpPost;
	}
}
