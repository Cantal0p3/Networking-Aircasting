package pl.llp.aircasting.IOTExtension;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.util.Constants;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

// OK not changing names right now, going to go back and try
// doing an accelerometer reader

public class AccelerometerReader extends IOTSensor implements SensorEventListener {

	/**
	 * 
	 * Author: Joe Zuhusky November/December 2016 Foundations of Networks 
	 * THIS IS AN EXTENSION CLASS!!!
	 * 
	 */
	// GLOBAL SENSOR INFO
	// For x,y,z independent sensor info, look at code below
	public static final String SYMBOL = "m/s^2";
	public static final String UNIT = "meters per second squared";
	public static final String MEASUREMENT_TYPE = "Cartesian Acceleration";
	public static final String SHORT_TYPE = "m/s^2";
	public static final String SENSOR_NAME = "Phone Accelerometer";
	public static final String SENSOR_PACKAGE_NAME = "Builtin";
	public static final String SENSOR_ADDRESS_BUILTIN = "none";

	// Can change these as needed for GUI color differentials
	public static final int VERY_LOW  = -30;
	public static final int LOW       = -15;
	public static final int MID       = 0;
	public static final int HIGH      = 15;
	public static final int VERY_HIGH = 30;
	
	/**
	 * 
	 * @param context ->
	 * I need the context from the Service, which is the context from the 
	 * Application. I need it because it provides the system service 
	 * of a Sensor Service. The original App didnt have this because it didnt 
	 * use any of these types of sensors -> just an audio reader which is NOT a sensor
	 * is it a media device :P
	 */
	
	private EventBus eventBus;
	private Context context;
	private SensorManager sm; // Object that android provides for access to Accelerometer
	
	public AccelerometerReader(Context context, EventBus eb) {
		this.context  = context;
		this.eventBus = eb;
	}

	// Sensor Objects in case they are needed....
	public static final Sensor sensorx = new Sensor(
			SENSOR_PACKAGE_NAME, SENSOR_NAME+"_x", MEASUREMENT_TYPE+" x", SHORT_TYPE, UNIT,
			SYMBOL, VERY_LOW, LOW, MID, HIGH, VERY_HIGH, SENSOR_ADDRESS_BUILTIN);

	public static final Sensor sensory = new Sensor(
			SENSOR_PACKAGE_NAME, SENSOR_NAME+"_y", MEASUREMENT_TYPE+" y", SHORT_TYPE, UNIT,
			SYMBOL, VERY_LOW, LOW, MID, HIGH, VERY_HIGH, SENSOR_ADDRESS_BUILTIN);

	public static final Sensor sensorz = new Sensor(
			SENSOR_PACKAGE_NAME, SENSOR_NAME+"_z", MEASUREMENT_TYPE+" z", SHORT_TYPE, UNIT,
			SYMBOL, VERY_LOW, LOW, MID, HIGH, VERY_HIGH, SENSOR_ADDRESS_BUILTIN);

	// ------------- SOME DATA FOR THIS CLASS ------------------
	long lastUpdate = 0;// the time since the last update-> for sampling
						// purposes (see onSensorChanged)

	// Variables for accelerometer readings...
	private float x = 0;
	private float y = 0;
	private float z = 0;
	
	// Boolean running flags for 3 cartesian sensors
	boolean xrunning = false;
	boolean yrunning = false;
	boolean zrunning = false;
	
	int ms_between_samples_acc = 500; // SAMPLING RATE
	
	// DEBUG TAG (who cares?)
	private static final String TAG = Constants.TAG + "/Accelerometer";
	
	/**
	 * @return A Sensor representing the internal accelerometer
	 */
	public static Sensor getSensorX() {
		return sensorx;
	}
	public static Sensor getSensorY() {
		return sensory;
	}
	
	public static Sensor getSensorZ() {
		return sensorz;
	}
	
	/**
	 * IMPLEMENTING THE IOT SENSOR API HERE
	 */
	public void startSensing(){
		running = true;
		sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		sm.registerListener(this, sm.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		Log.i(TAG, "AccelREADER: Start");
		startX(); startY(); startZ();
	}
	
	public void stopSensing(){
		Log.i(TAG, "Reader: Signal Stop");
		sm.unregisterListener(this);
		Log.i(TAG, "BattREADER: STOP");
		running = false;
		stopX(); stopY(); stopZ();
	}
	
	public String accVals() {
		return "(" + String.format("%.2f", x).toString() + "," + String.format("%.2f", y).toString() + ","
				+ String.format("%.2f", z).toString() + ")";
	}
	
	/**
	 * 
	 * @param evt
	 * On event, run this method to sample data
	 */
	
	public void readerRun(SensorEvent evt) {

		if (evt.sensor.getType() == android.hardware.Sensor.TYPE_ACCELEROMETER) {
			x = evt.values[0];
			y = evt.values[1];
			z = evt.values[2];
			// Generate sensor events & post to bus if dim running
			pl.llp.aircasting.model.events.SensorEvent xevent 
			= new pl.llp.aircasting.model.events.SensorEvent(
					SENSOR_PACKAGE_NAME, SENSOR_NAME+" x", MEASUREMENT_TYPE+" x", SHORT_TYPE, UNIT,
					SYMBOL, VERY_LOW, LOW, MID, HIGH, VERY_HIGH, (double) x);
			pl.llp.aircasting.model.events.SensorEvent yevent 
			= new pl.llp.aircasting.model.events.SensorEvent(
					SENSOR_PACKAGE_NAME, SENSOR_NAME+" y", MEASUREMENT_TYPE+" y", SHORT_TYPE, UNIT,
					SYMBOL, VERY_LOW, LOW, MID, HIGH, VERY_HIGH, (double) y);
			pl.llp.aircasting.model.events.SensorEvent zevent 
			= new pl.llp.aircasting.model.events.SensorEvent(
					SENSOR_PACKAGE_NAME, SENSOR_NAME+" z", MEASUREMENT_TYPE+" z", SHORT_TYPE, UNIT,
					SYMBOL, VERY_LOW, LOW, MID, HIGH, VERY_HIGH, (double) z);
			if ( xrunning )
				eventBus.post(xevent);
			if ( yrunning )
				eventBus.post(yevent);
			if ( zrunning )
				eventBus.post(zevent);
		}
	}
	
	public void startX(){
		xrunning = true;
	}

	public void startY(){
		yrunning = true;
	}

	public void startZ(){
		zrunning = true;
	}
	
	public void stopX(){
		xrunning = false;
	}

	public void stopY(){
		yrunning = false;
	}

	public void stopZ(){
		zrunning = false;
	}

	
	public boolean SensorManagerNull() {
		return (sm == null);
	}

	public String lastUpdated() {
		return lastUpdate + "";
	}

	@Override
	public void setSamplingRate(int rate) {
		ms_between_samples_acc = 1000/rate;
	}
	
	/**
	 * BELOW METHODS REQUIRED BY THE 
	 * 
	 * "SensorEventListener" android interface....
	 */
	@Override
	public void onSensorChanged(SensorEvent arg0) {

		// See if enuf time has passed...
		long curTime = System.currentTimeMillis();
		if ((curTime - lastUpdate) > ms_between_samples_acc) {
			long diffTime = (curTime - lastUpdate);
			lastUpdate = curTime;
			readerRun(arg0);
		}
	}
	/**
	 * This method is just requred by the interface, not dealing with it ATM
	 */
	@Override
	public void onAccuracyChanged(android.hardware.Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
	}
	
}
