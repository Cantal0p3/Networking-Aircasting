package pl.llp.aircasting.IOTExtension;

import com.google.gson.annotations.Expose;

public class KeepAliveData {
	@Expose
	private String IMEI;
	@Expose
	private float batteryLife;
	@Expose
	private boolean keepAliveStatus;
	@Expose
	private double longitude;
	@Expose
	private double latitude;
	@Expose
	private String ip;
	
	public String getIMEI() {
		return IMEI;
	}
	public void setIMEI(String iMEI) {
		IMEI = iMEI;
	}
	public float getBatteryLife() {
		return batteryLife;
	}
	public void setBatteryLife(float batteryLife) {
		this.batteryLife = batteryLife;
	}
	public boolean isKeepAliveStatus() {
		return keepAliveStatus;
	}
	public void setKeepAliveStatus(boolean keepAliveStatus) {
		this.keepAliveStatus = keepAliveStatus;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	

}
