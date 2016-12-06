package pl.llp.aircasting.IOTExtension;

import pl.llp.aircasting.model.MeasurementStream;

/**
 * 
 * @author josephzuhusky
 * Foundations of Networks course
 * IOT Project
 * November/Decemeber 2016
 *
 */

public abstract class IOTSensor {
	
	public boolean running = false;

	public abstract void startSensing();
	public abstract void stopSensing();
	/**
	 * @param rate
	 *            at which this reader will sample accelerometer data UNITS!!!!!
	 *            => You should pass in a rate that has units of SAMPLES PER
	 *            SECOND!!!!!!!
	 */
	public abstract void setSamplingRate(int rate);
	public boolean isRunning(){
		return running;
	}

	
}
