package pl.llp.aircasting.IOTExtension;


//import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.RecordWithoutGPSAlert;
import pl.llp.aircasting.activity.SaveSessionActivity;
import pl.llp.aircasting.activity.events.SessionChangeEvent;
import pl.llp.aircasting.activity.events.SessionStartedEvent;
import pl.llp.aircasting.activity.events.SessionStoppedEvent;
import pl.llp.aircasting.api.data.SyncResponse;
import pl.llp.aircasting.helper.LocationHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.sensor.SensorStoppedEvent;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.sync.SessionSyncException;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.util.http.Status;
import roboguice.service.RoboService;

import java.util.Date;

import static pl.llp.aircasting.util.http.HttpBuilder.http;

import java.net.Socket;
import java.util.*;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Scopes;
import com.google.gson.Gson;

/**
 * This will eventually become the IOT Service.....
 * @author josephzuhusky & Soumie Kumar
 * 
 * 	
 * IOT Wrapper service (extension to AirCasting Android Application)
 * Authors: Joe Zuhusky & Soumie Kumar
 * Foundations of Networks and Mobile Systems
 * December 2016	
 *
 */

//@SuppressLint("DefaultLocale")
public class ExtendedSensorService extends RoboService implements IOTWrapper {
	

	/**
	 * --------------------------------------------------------------------
	 * 						BEGIN SERVICE VARS AND DATA
	 * --------------------------------------------------------------------
	 */
	
	@Inject EventBus eventBus;
	@Inject SessionRepository sessionRepository;
	
	Gson gson;
	Context context;

	// Some current IOT Sensor readers
	// Not using AirCasting Sensors at the moment...
	private AccelerometerReader accelReader;
	private BatteryReader       battReader;
	private CompassReader       compassReader;
	private List<String> availSensors = new ArrayList<String>(
		    Arrays.asList("accelerometer-all",
		    		"accelerometer-x",
		    		"accelerometer-y",
		    		"accelerometer-z",
		    		"compass",
		    		"battery-level",
		    		"sound-level"
		    		));
	private Map<String,IOTSensor> iotSensorMap = new HashMap<String,IOTSensor>();
	
	Date on_service_created;
	
	/**
	 * Samples = A simple object to hold samples from the 
	 * Current readers -> I.e. the accelReader and BattReader
	 */

	/**
	 * Some Sync state info....
	 */
	// MOST OF THIS STUFF NOT USED RIGHT NOW
	private final String SYNC_BATCH = "SYNC_BATCH";   // Sync on server command
	private final String SYNC_AUTO  = "SYNC_AUTO";    // Sync every at every time N
	
	// Not using stuff below at the moment....
	private String SYNC_MODE      = SYNC_AUTO; // by default...
	private float  sync_interval  = 30000;     // sync every minute right now... (if state is ONINTERVAL)
	private Thread autoSyncThread = new Thread(new Runnable(){
		@Override
		public void run(){
			syncLoop(); // this will call a method that runs a loop for Automatic syncing
		}
	});
	
	
	//---------------------STATE VARIABLES-------------------------
	// INFO for FSM / FSA that will be controlled by Server...
	
	private boolean batchSync = true;  // Sync on command
	private boolean autoSync  = false;
	
	private boolean isSamplingData = false;
	//private boolean 
	
	
	// Maybe provide a list of available sensors???
	// NEED A LIST OF AVAILABLE SENSORS>...
	
	/**
	 * --------------------------------------------------------------------
	 * 						END SERVICE VARS AND DATA
	 * 				
	 * 						BEGIN SERVICE & IOT API METHODS
	 * --------------------------------------------------------------------
	 */

	// SERVICE METHOD Implemented
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	

	// SERVICE METHOD
	@Override
	public void onCreate(){
		super.onCreate();
		eventBus.register(this); // hook up with EventBus...
		gson       = new Gson();
		context    = getApplicationContext();
		
		on_service_created = new Date(); // create a new date object of the time when started....
		
		// Create Sensor Reader objects
		accelReader   = new AccelerometerReader(this, eventBus);
		battReader    = new BatteryReader(this,eventBus);
		compassReader = new CompassReader(this,eventBus);
		
		// Create an IOT Sensor Map for N number of Additional Sensors
		iotSensorMap.put("accelerometer-all", accelReader);
		iotSensorMap.put("batterReader", battReader);
		iotSensorMap.put("compassReader", compassReader);
		
		/**
		 * NOTE TO SELF-> I think we're going to have to hard code most of the 
		 * Toggle commands for the Bluetooth sensor and audioreader that
		 * are already in place.... Lets save this for another time....
		 */

		StartSensing(); // FOR NOW>........

		// Show a Toast
		Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
               Toast.makeText(
            		   getApplicationContext(),
            		   "IOT Service Started",
            		   Toast.LENGTH_SHORT)
               .show();
            }
        });
        
        
	}
	
	// SERVICE METHOD
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		long lastUpdate = 0;
        return START_STICKY; // -> This means that the service will stay alive until I kill it (or app does)
    }
	
	// SERVICE METHOD
	@Override
	public void onDestroy() {
		super.onDestroy();
		StopSensing();
		eventBus.unregister(this);
	}

	//BEGIN IOT API METHODS
	
	/**
	 * I think that the logic for this method
	 * should be to toggle->ON Aircasting for all sensors 
	 * Unless another StartSensing method is called with
	 * either a List<SensorNames<String>> or a simple string sensor name..
	 */

	public void StartSensing(){
		/*if (SYNC_MODE.equals("SYNC_AUTO")){
			autoSyncThread.start();
		} */
		
		// eventBus.post(new IOTToggleAirCastingEvent()); //-> THIS SHOULD DO IT
		// TAKE A LOOK AT ADDED SUBSCRIBER METHOD IN BUTTONSACTIVITY
		
		if (!compassReader.isRunning()){
			compassReader.startSensing();
		}
		if (!accelReader.isRunning() ){
			accelReader.startSensing();
		}
		if (!battReader.isRunning() ){
			battReader.startSensing();
		}
	}
	
	/**
	 * Same thing here as above, I think that logically, this method should toggle aircasting
	 */
	public void StopSensing(){
/*		
		if (accelReader.isRunning()){
			accelReader.stopSensing();
		}
		if (battReader.isRunning()){
			battReader.stopSensing();
		}
		if (compassReader.isRunning()){
			compassReader.stopSensing();
		}
		/*if (SYNC_MODE.equals("SYNC_AUTO")){
			try {
				autoSyncThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} */
	}
	

	/**
	 * These two methods below havent been used,
	 * but are more like prototypes
	 * for the idea that im going after....
	 * 
	 * Something a little more complex might be required
	 * for toggling acc-x,y,z individually....
	 */
	@Override
	public void SensorActivate(String name) {
		name = name.toLowerCase(); // this should be conventional???
		if (iotSensorMap.containsKey(name)){
			IOTSensor current = iotSensorMap.get(name);
			current.startSensing();
		}
	}

	@Override
	public void SensorDeactivate(String name) {
		name = name.toLowerCase(); // this should be conventional???
		if (iotSensorMap.containsKey(name)){
			IOTSensor current = iotSensorMap.get(name);
			current.stopSensing();
		}
		
	}
	
	/**
	 * 
	 * @param sensorName -> Specific Sensor 
	 * being told to turn itself off...
	 */
	public void StopSensing(String sensorName){
		// loop thru sensors and pick out one to stop
	}
	
	/**
	 * API exposes the ability to send all cached data
	 * between 2 times specified by the Server
	 * 
	 * @param d1 from this time
	 * @param d2 to this time
	 */
	
	public void sendAllData(Date d1, Date d2){
		// Query Session Repo
		// And send over valid data between the times specified
	}
	
	// Same idea as sendAllData, but use some sort of 
	// compression alg
	public void sendCompressedData(Date d1, Date d2){
		// Query Session Repo
		// And send over valid data between the times specified
		// The try to compress and send....
	}
	
	/**
	 * Same idea as above 2 functions
	 * @param t1
	 * @param t2
	 * @param T -> MUST USE UNITS OF SAMPLES PER SECOND!!!
	 */
	public void sendSampledData(Date t1, Date t2, int T){
		// Put logic here

	}
	
	// not really sure what this does quite yet
	public void addSensor(){
		// put logic???
	}
	
	public void SendDatafromSpecificSensor (Date t1, Date t2, String sensor_name){
		// put logic here
	}
	
	@Subscribe 
	public void OnSensorEvent(SessionStoppedEvent e){

	}
	
	public void sendSampledData(){
		// put logic here
	}

	// not so clear on this one
	public void sendControl(IOTSensor X){
		// put logic here
	}
	

	int numSyncs  =0; // set a limit for time being on number of syncs
	long lastSync =0;
	private void syncLoop(){
/*		while (SYNC_MODE.equals("SYNC_AUTO") && (accelReader.isRunning() || battReader.isRunning()) ){
			long curTime = System.currentTimeMillis();
			if ((curTime - lastSync) > sync_interval) {
				lastSync = curTime;
				sendAllData(this.on_service_created,new Date());
				numSyncs++;
				if (numSyncs == 20){
					break;
				}
			}
		}
		this.stopSelf(); */
	}
		
	@Override
	public void keepAlive() {
		// TODO Auto-generated method stub
		
	}
	
}
