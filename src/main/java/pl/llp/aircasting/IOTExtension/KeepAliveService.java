package pl.llp.aircasting.IOTExtension;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ExecutionError;
import com.google.gson.Gson;
import com.google.inject.Inject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.widget.Toast;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.helper.LocationHelper;
import roboguice.service.RoboService;

/*
 * @Author Soumie Kumar
 * For Foundations of Networking class in Fall 2016
 * 
 */
public class KeepAliveService extends RoboService{
	
	@Inject
	Gson gson;
	@Inject
	ServerConnectionManager connManager;
	@Inject
	LocationHelper locationHelper;
	@Inject
	Context context;
	private boolean isServiceActive;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		super.onStartCommand(intent, flags, startId);
		isServiceActive = true;
		//sendKeepAliveData();
		/*try {
			Runnable runnable = new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					sendKeepAliveData();
					
				}
			};
			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
			executorService.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
		}*/
		
		final Handler mHandler = new Handler();
		mHandler.postDelayed(new Runnable()
		{
		public void run() {
		//This will be executed on thread using Looper.
			
			if(isServiceActive) {
				sendKeepAliveData();
				mHandler.postDelayed(this, 5000);
			}
			}
		},5000);
		
		return START_NOT_STICKY;
	}
	
	private void sendKeepAliveData() {
		KeepAliveData keepAliveData = new KeepAliveData();
		keepAliveData.setIMEI(getIMEI());
		keepAliveData.setBatteryLife(getBatteryLife());
		keepAliveData.setIp(getPhoneIP());
		keepAliveData.setKeepAliveStatus(getKeepAliveStatus());
		keepAliveData.setLongitude(getLocation().getLongitude());
		keepAliveData.setLatitude(getLocation().getLatitude());
		String keepAliveDataJson = gson.toJson(keepAliveData);
		connManager.sendDataToServer(keepAliveDataJson);
	}
	
	private String getIMEI() {
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}
	
	private Location getLocation() {
		Location location = locationHelper.getLastLocation();
		return location;
	}
	
	private String getPhoneIP() {
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		//TODO get ip from cellular
		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
		return ip;
	}
	
	private float getBatteryLife() {
		Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	    int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
	    float batteryLife = ((float) level / (float) scale) * 100.0f;
	    return batteryLife;
	}
	
	private boolean getKeepAliveStatus() {
		//TODO check if any other phone status data needs to be sent
		return true;
	}
	
	@Override
	  public void onDestroy()
	  {
	    super.onDestroy();
	    isServiceActive = false;
	    Intents.stopKeepAliveService(context);
	  }


}
