package com.perfspeed.collector.geo;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

public class GeoIPLocationServiceTest {

	private final static String GEO_CITY_BINARY_PATH = "/Users/skalaise/Downloads/geo_db/GeoLite2-City.mmdb";
	static DatabaseReader dbReader;
	
	static {
		try{
			final File geoDB = new File(GEO_CITY_BINARY_PATH);
			dbReader =  new DatabaseReader.Builder(geoDB).build();
		}catch (Exception e) {
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
	
	public static void main(String[] args) {
		GeoInfo info = getGeoInfo("203.145.158.169");
		System.out.println(info);
	}
}
