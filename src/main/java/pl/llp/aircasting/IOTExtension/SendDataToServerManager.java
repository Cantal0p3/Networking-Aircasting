package pl.llp.aircasting.IOTExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.inject.Inject;

import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.storage.repository.SessionRepository;

public class SendDataToServerManager {
	@Inject ServerConnectionManager connectionManager;
	@Inject SessionRepository sessionRepository;
	@Inject Gson gson;
	
	public void sendAllData(Date startDate, Date endDate){
		List<Session> sessionData = sessionRepository.allCompleteSessions();
		List<Session> filteredSessionData = new ArrayList<Session>();
		for (Session s: sessionData) {
			if (s.getStart().after(startDate) && s.getStart().before(endDate)){
				filteredSessionData.add(s);
			}
		}
		String allSessionDataBetweenDates = gson.toJson(filteredSessionData);
		connectionManager.sendDataToServer(allSessionDataBetweenDates);
		
	}
	
	public void SendDatafromSpecificSensor (Date startDate, Date endDate, String sensor_name){
		List<Session> sessionData = sessionRepository.allCompleteSessions();
		List<Session> filteredSessionData = new ArrayList<Session>();
		for (Session s: sessionData) {
			if (s.getStart().after(startDate) && s.getStart().before(endDate)){
				for(MeasurementStream m:s.getMeasurementStreams()) {
					if (m.getSensorName().equalsIgnoreCase(sensor_name))
						filteredSessionData.add(s);
				}
				
			}
		}
		String allSessionDataBetweenDates = gson.toJson(filteredSessionData);
		connectionManager.sendDataToServer(allSessionDataBetweenDates);
	}
	
	public void sendAllData(){
		List<Session> sessionData = sessionRepository.allCompleteSessions();
		String allSessionDataBetweenDates = gson.toJson(sessionData);
		connectionManager.sendDataToServer(allSessionDataBetweenDates);		
	}

}
