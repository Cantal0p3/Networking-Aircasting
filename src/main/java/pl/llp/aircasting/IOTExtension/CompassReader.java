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

public class CompassReader extends IOTSensor implements SensorEventListener {

	/**
	 * 
	 * Author: Joe Zuhusky November/December 2016 Foundations of Networks 
	 * THIS IS AN EXTENSION CLASS!!!
	 * 
	 */
	// GLOBAL SENSOR INFO
	// For x,y,z independent sensor info, look at code below
	public static final String SYMBOL              = ""+(char) 0x00B0;
	public static final String UNIT                = "DEGREES";
	public static final String MEASUREMENT_TYPE    = "Compass Heading";
	public static final String SHORT_TYPE          = "deg"; // I think this does degrees symbol in android?????????
	public static final String SENSOR_NAME         = "Phone Compass";
	public static final String SENSOR_PACKAGE_NAME = "Builtin";
	public static final String SENSOR_ADDRESS_BUILTIN = "none";

	// Can change these as needed for GUI color differentials
	public static final int VERY_LOW  = 0;
	public static final int LOW       = 72;
	public static final int MID       = 144;
	public static final int HIGH      = 216;
	public static final int VERY_HIGH = 288;

	
	private EventBus eventBus;
	private Context context;
	private SensorManager sm; // Object that android provides for access to Accelerometer
	
	public CompassReader(Context context, EventBus eb) {
		this.context  = context;
		this.eventBus = eb;
	}

	// Sensor Objects in case they are needed....
	public static final Sensor sensor = new Sensor(
			SENSOR_PACKAGE_NAME, SENSOR_NAME, MEASUREMENT_TYPE, SHORT_TYPE, UNIT,
			SYMBOL, VERY_LOW, LOW, MID, HIGH, VERY_HIGH, SENSOR_ADDRESS_BUILTIN);

	// ------------- SOME DATA FOR THIS CLASS ------------------
	long lastUpdate   = 0;// Last sensor update -> Sampling purposes
	private float deg = 0;
	int ms_between_samples_acc = 200; // SAMPLING RATE
	
	// DEBUG TAG (who cares?)
	private static final String TAG = Constants.TAG + "/Compass";
	
	/**
	 * @return A Sensor representing the internal accelerometer
	 */
	public static Sensor getSensorX() {
		return sensor;
	}
	
	/**
	 * IMPLEMENTING THE IOT SENSOR API HERE
	 */
	public void startSensing(){
		running = true;
		sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		sm.registerListener(this, sm.getDefaultSensor(android.hardware.Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_NORMAL);
		Log.i(TAG, "AccelREADER: Start");
	}
	
	public void stopSensing(){
		Log.i(TAG, "CompassReader: Signal Stop");
		sm.unregisterListener(this);
		Log.i(TAG, "CompassReader: STOP");
		running = false;
	}
	
	public String accVals() {
		return deg + SHORT_TYPE;
	}
	
	/**
	 * 
	 * @param evt
	 * On event, run this method to sample data
	 */
	
	public void readerRun(SensorEvent evt) {

		if (evt.sensor.getType() == android.hardware.Sensor.TYPE_ORIENTATION) {
			deg = evt.values[0];
			// Generate sensor events & post to bus if dim running
			pl.llp.aircasting.model.events.SensorEvent event 
			= new pl.llp.aircasting.model.events.SensorEvent(
					SENSOR_PACKAGE_NAME, SENSOR_NAME, MEASUREMENT_TYPE, SHORT_TYPE, UNIT,
					SYMBOL, VERY_LOW, LOW, MID, HIGH, VERY_HIGH, (double) deg);
			eventBus.post(event);
		}
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
