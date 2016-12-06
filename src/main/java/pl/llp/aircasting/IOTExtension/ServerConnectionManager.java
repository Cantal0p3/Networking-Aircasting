package pl.llp.aircasting.IOTExtension;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.inject.Inject;

import pl.llp.aircasting.helper.SettingsHelper;

public class ServerConnectionManager {

	@Inject
	SettingsHelper helper;

	private final String ACCEPT_HEADER = "Accept";
	private final String ACCEPT_HEADER_VALUE = "application/json";
	private final String CONTENT_HEADER = "Content-type";
	private final String CONTENT_HEADER_VALUE = "application/json";

	public void sendDataToServer(String jsonData) {

		DefaultHttpClient httpClient = new DefaultHttpClient();
		String hostUrl = "http://" + helper.getBackendURL() + ":" + helper.getBackendPort() + "/";
		HttpPost post = new HttpPost(hostUrl);
		try {
			StringEntity stringEntity = new StringEntity(jsonData);
			post.setEntity(stringEntity);
			post.setHeader(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
			post.setHeader(CONTENT_HEADER, CONTENT_HEADER_VALUE);
			HttpResponse response = httpClient.execute(post);
			HttpEntity httpEntity = response.getEntity();
			String theString = IOUtils.toString(httpEntity.getContent(), StandardCharsets.UTF_8); 
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
