package pl.llp.aircasting.IOTExtension;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.media.AudioRecord;

public class BatteryReader extends IOTSensor {
		
	public static final String SYMBOL = "%";
	public static final String UNIT   = "n/a";
	public static final String MEASUREMENT_TYPE = "Phone Battery Level";
	public static final String SHORT_TYPE       = "%";
	public static final String SENSOR_NAME      = "Phone BatteryReader";
	public static final String SENSOR_PACKAGE_NAME    = "Builtin";
	public static final String SENSOR_ADDRESS_BUILTIN = "none";

	public static final int VERY_LOW = 10;
	public static final int LOW      = 25;
	public static final int MID      = 50;
	public static final int HIGH     = 75;
	public static final int VERY_HIGH = 90;
	
	// DEBUG TAG
	private static final String TAG = Constants.TAG + "/Accelerometer";
	
	private IntentFilter iFilter;
	private Intent       batteryStatus;
	private Context      context;
	private EventBus     eventBus;
	
	private Thread battReaderThread; // battery Reader isn't hardware SensorEventListener->need thread

	long lastUpdate         = -100000; // So that the reader updates on start....
	long ms_between_samples = 60000; // Default Sampling Rate is every minute
	float batteryPct        = -1;
	
	// Create sensor Object if need be
	public static final Sensor sensor = new Sensor(SENSOR_PACKAGE_NAME, SENSOR_NAME, MEASUREMENT_TYPE, SHORT_TYPE, UNIT,
				SYMBOL, VERY_LOW, LOW, MID, HIGH, VERY_HIGH, SENSOR_ADDRESS_BUILTIN);
	
	public BatteryReader(Context context, EventBus eb){
		this.context = context;
		this.eventBus = eb;
	}
	
	@Override
	public void startSensing() {
		iFilter       = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		batteryStatus = context.registerReceiver(null, iFilter);
		running       = true;
		
		battReaderThread = new Thread(new Runnable(){
			@Override
			public void run(){
				while (running){
					long curTime = System.currentTimeMillis();
					if ((curTime - lastUpdate) > ms_between_samples) {
						lastUpdate = curTime;
						int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
						int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
						batteryPct = level / (float) scale * 100;

						Handler mHandler = new Handler(Looper.getMainLooper());
						pl.llp.aircasting.model.events.SensorEvent event 
						= new pl.llp.aircasting.model.events.SensorEvent(
								SENSOR_PACKAGE_NAME, SENSOR_NAME, MEASUREMENT_TYPE, SHORT_TYPE, UNIT,
								SYMBOL, VERY_LOW, LOW, MID, HIGH, VERY_HIGH, (double) batteryPct);
						eventBus.post(event);

					}
				}
			}
		});	
		battReaderThread.start();
	}

	@Override
	public void stopSensing() {
		iFilter = null;
		running = false;
		batteryStatus = null;
		try {
			battReaderThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setSamplingRate(int rate) {
		ms_between_samples = 1000/rate;
	}
	
}
