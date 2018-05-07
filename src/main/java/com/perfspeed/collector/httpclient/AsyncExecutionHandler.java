package com.perfspeed.collector.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.perfspeed.collector.utils.HttpUtils;


public class AsyncExecutionHandler {
	private static final Logger LOG = LoggerFactory.getLogger(AsyncExecutionHandler.class);
	
	public static void executeCall(final CloseableHttpAsyncClient client, final HttpPost request) {
		try{
			client.execute(request, new FutureCallback<HttpResponse>() {
		        public void completed(final HttpResponse response) {
		        	try {
		        		int statusCode = response.getStatusLine().getStatusCode();
		        		LOG.info("Status Code : {}, Request URI : {} ", statusCode, request.getURI());
			            if(statusCode != HttpStatus.SC_OK)
			             HttpClientUtils.closeQuietly(response);
					} catch (Exception e) {
						LOG.error("error while consuming resp ", e);
					}
		        }
		        public void failed(final Exception ex) {
		        	LOG.info("Failed to post events to destination EndPoint :  {} , Request : {} ", request.getURI(), HttpUtils.getIncomingRequestEntity(request.getEntity()),  ex);
		        }
		        public void cancelled() {
		        	LOG.error("task cancelled by http client ");
		        }
		    });
		}catch (Exception e) {
			LOG.error(" Error while executing POST request : {} , EndPoint : {} ", HttpUtils.getIncomingRequestEntity(request.getEntity()), request.getURI(), e);
		}
	}
}