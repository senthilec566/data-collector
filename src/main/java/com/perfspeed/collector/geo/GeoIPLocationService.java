package com.perfspeed.collector.geo;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.perfspeed.collector.SslServerContext;
import com.perfspeed.collector.utils.CollectorUtils;

public class GeoIPLocationService {
	
	private static final Logger LOG = LoggerFactory.getLogger(SslServerContext.class);
	
	private final static String GEO_CITY_BINARY_PATH = CollectorUtils.getGeoDBPath();
	static DatabaseReader dbReader;
	
	static {
		try{
			final File geoDB = new File(GEO_CITY_BINARY_PATH);
			dbReader =  new DatabaseReader.Builder(geoDB).build();
		}catch (Exception e) {
			LOG.error("Failed to Load Geo DB ", e);
		}
	}
	
	public static GeoInfo getGeoInfo(final String ipAddr){
		final GeoInfo geoInfo = new GeoInfo();
		try {
			final InetAddress ipAddress = InetAddress.getByName(ipAddr);
			final CityResponse response = dbReader.city(ipAddress);
			geoInfo.setCity(response.getCity().getName());
			geoInfo.setIpAddress(ipAddress.getHostAddress());
			geoInfo.setCountry(response.getCountry().getName());
			geoInfo.setZip(response.getPostal().getCode());
			geoInfo.setLattitude(String.valueOf(response.getLocation().getLatitude()));
			geoInfo.setLongitude(String.valueOf(response.getLocation().getLongitude()));
		} catch ( IOException | GeoIp2Exception e) {
			e.printStackTrace();
		}
		return geoInfo;
	}
	
	public static void addGeo(final String ipAddr, final Map<String,String> dataMap){
		try {
			final InetAddress ipAddress = InetAddress.getByName(ipAddr);
			final CityResponse response = dbReader.city(ipAddress);
			dataMap.put("city", response.getCity().getName());
			dataMap.put("ipaddress", ipAddress.getHostAddress());
			dataMap.put("country", response.getCountry().getName());
			dataMap.put("zip", response.getPostal().getCode());
			dataMap.put("lat", String.valueOf(response.getLocation().getLatitude()));
			dataMap.put("long", String.valueOf(response.getLocation().getLongitude()));
		} catch ( IOException | GeoIp2Exception e) {
			e.printStackTrace();
		}
	}
	
}
