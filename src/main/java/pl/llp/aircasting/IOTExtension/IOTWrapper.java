package pl.llp.aircasting.IOTExtension;

import static pl.llp.aircasting.util.http.HttpBuilder.http;

import java.util.Date;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import pl.llp.aircasting.api.data.SyncResponse;

public interface IOTWrapper {

	public void keepAlive();
	public void StartSensing(); // Start Recording Data
	public void StopSensing();  // Stop Recording Data
	public void SensorActivate(String name); // these toggle individual sensors...
	public void SensorDeactivate(String name); 	
	/**
	 * API exposes the ability to send all cached data
	 * between 2 times specified by the Server
	 * 
	 * @param d1 from this time
	 * @param d2 to this time
	 */
	
	public void sendAllData(Date d1, Date d2);
	public void sendSampledData(Date t1, Date t2, int T);
	public void sendSampledData();
	public void sendCompressedData(Date d1, Date d2);
	/**
	 * Same idea as above 2 functions
	 * @param t1
	 * @param t2
	 * @param T -> MUST USE UNITS OF SAMPLES PER SECOND!!!
	 */
	
	public void addSensor(); // 
	public void SendDatafromSpecificSensor (Date t1, Date t2, String sensor_name);
	public void sendControl(IOTSensor X);
	
}
