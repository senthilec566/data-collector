package com.perfspeed.collector.geo;

public class GeoInfo {

	private String ipAddress;
	private String country;
	private String city;
	private String zip;
	private String lattitude;
	private String longitude;
	
	public String getIpAddress() {
		return ipAddress;
	}
	public String getCountry() {
		return country;
	}
	public String getCity() {
		return city;
	}
	public String getZip() {
		return zip;
	}
	public String getLattitude() {
		return lattitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public void setLattitude(String lattitude) {
		this.lattitude = lattitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	@Override
	public String toString() {
		return "GeoInfo [ipAddress=" + ipAddress + ", country=" + country + ", city=" + city + ", zip=" + zip
				+ ", lattitude=" + lattitude + ", longitude=" + longitude + "]";
	}

}
