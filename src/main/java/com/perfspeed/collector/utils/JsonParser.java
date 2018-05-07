package com.perfspeed.collector.utils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParser {

	private static final Logger LOG = LoggerFactory.getLogger(JsonParser.class);
	private final static ObjectMapper jsonParser = new ObjectMapper();
	
	static{
		jsonParser.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		jsonParser.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonParser.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
	}
	/**
	 * Convert Object to JSON String 
	 * @param obj which object to convert
	 * @return json string
	 */
	public static <T> String objectToJson( final T obj) {
		try {
			return  jsonParser.writeValueAsString(obj);
		} catch (Exception e) {
			LOG.error(" Cannot convert object to json str ", e);
		}
		return "";
	}
	
	
	/**
	 * Convert JSON to StreamDetailsInfo
	 * @param <T>
	 * @param obj which object to convert
	 * @return json string
	 */
	public static <T> T jsonToObj( final String json, final Class<T> clazz ) {
		try {
			return jsonParser.readValue(json, clazz);
		} catch (Exception e) {
			LOG.error(" Cannot convert json to  obj  ", e);
		}
		return null;
	}
	

	/**
	 * Map to Json 
	 * @param map
	 * @return json string
	 */
	public static String map2Json( final Map<String,String> map ){
		try{
			return jsonParser.writeValueAsString(map);
		}catch (Exception e) {
			LOG.error(" Cannot convert map to json str ", e);
		}
		return "";
	}
}
