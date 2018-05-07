package com.perfspeed.collector.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StringUtils {

	
	public static Map<String, String> parseMap(final String input) {
        final Map<String, String> map = new HashMap<String, String>();
        for (final String pair : input.split("&")) {
        	if( Objects.nonNull(pair) && !pair.isEmpty() ){
        		 final String[] kv = pair.split("=");
                 if( Objects.nonNull(kv) && kv.length > 1  && Objects.nonNull(kv[0]) && !kv[0].isEmpty() 
                		 && Objects.nonNull(kv[1]) && !kv[1].isEmpty())
                 		map.put(kv[0], kv[1]);
        	}
        }
        return map;
    }
}
