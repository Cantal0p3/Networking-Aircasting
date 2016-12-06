package pl.llp.aircasting.IOTExtension;

import java.util.*;

public class Samples {
	
	String type;
	String device      = "Motorola-TC55ch";
	String description = "IOT-TEST || Sensors: ACC, BATT";
	Set<AccelerometerSample> acc_samples;
	Set<BatterySample> batt_samples;
	
	class AccelerometerSample{
		final String description = "Accelerometer-Sample";
		float x,y,z;
		Date date;
		AccelerometerSample(float x, float y, float z){
			this.date = new Date();
			this.x=x;
			this.y=y;
			this.z=z;
		}
	}
	
	class BatterySample{
		final String description = "BatteryLevel-Sample";
		float batteryPct = -1;
		Date date;
		BatterySample(float value){
			this.date = new Date();
			batteryPct = value;
		}
	}
	
	
	public Samples(){
		this.acc_samples  = new HashSet<AccelerometerSample>();
		this.batt_samples = new HashSet<BatterySample>();
	}
	
	public void addAccSample(float x, float y, float z){
		acc_samples.add(new AccelerometerSample(x,y,z));
	}
	
	public void addBattSample(float value){
		batt_samples.add(new BatterySample(value));
	}
	
	

}
