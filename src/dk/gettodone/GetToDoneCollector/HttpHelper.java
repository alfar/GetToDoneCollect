package dk.gettodone.GetToDoneCollector;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.preference.PreferenceManager;
import android.util.Log;

public class HttpHelper {
	private Context mContext;
	private AndroidHttpClient mClient;

	public HttpHelper(Context ctx) {
		mContext = ctx;
	}

	private AndroidHttpClient getClient() {
		if (mClient == null)
		{
			mClient = AndroidHttpClient.newInstance("GetToDone Mobile v1.0");
		}
		return mClient;
	}

	private String postWithApiKey(String url, String data) throws ClientProtocolException, IOException	
	{		
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		HttpPost post = new HttpPost(String.format("http://www.gettodone.dk/api/%s%s", getApiKey(), url));
		post.setHeader("Accept", "application/json");
		post.setHeader("Content-type", "application/json; charset=UTF-8");
		post.setHeader("Content-Encoding", "UTF-8");
		ByteArrayEntity ent = new ByteArrayEntity(data.getBytes("UTF-8"));
		post.setEntity(ent);
		String response = getClient().execute(post, responseHandler);

		return response;
	}

	private String postWithoutApiKey(String url, String data) throws ClientProtocolException, IOException	
	{		
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		HttpPost post = new HttpPost(String.format("http://www.gettodone.dk/api%s", url));
		post.setHeader("Accept", "application/json");
		post.setHeader("Content-type", "application/json");
		post.setEntity(new StringEntity(data));
		String response = getClient().execute(post, responseHandler);

		return response;
	}

	private String mApiKey = "";
	public String getApiKey() {
		if (mApiKey.isEmpty())
		{
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			mApiKey = sharedPrefs.getString("api_key", "");
		}
		return mApiKey;
	}
	
	public void setApiKey(String key) {
		if (key != null) {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			sharedPrefs.edit().putString("api_key", key).commit();
		}
	}

	public void collect(String title) {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("Title", title);

			Runnable r = new PostHelper("/task/create", jsonObject.toString(), true);
			Thread t = new Thread(r);
			t.start();
			try{
				t.join();
			}
			catch (Exception e)
			{
			}
		} catch (JSONException e1) {
		}
	}

	private class PostHelper implements Runnable {
		public PostHelper(String url, String data, boolean requireApiKey)
		{
			mUrl = url;
			mData = data;
			mRequireApiKey = requireApiKey;
		}

		private String mUrl;
		private String mData;
		private String mResponse;
		private boolean mRequireApiKey;

		public String getResponse()
		{
			return mResponse;
		}

		public void run() {
			try {
				if (mRequireApiKey) {
					mResponse = postWithApiKey(mUrl, mData);
				} else {
					mResponse = postWithoutApiKey(mUrl, mData);
				}				
			} 
			catch (ClientProtocolException e) {
				Log.d("Unposted", e.getMessage());
			}
			catch (IOException e) {
			}
		}
	}

	public String login(String username, String password) {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("UserName", username);
			jsonObject.put("Password", password);

			PostHelper r = new PostHelper("/account/LogOn", jsonObject.toString(), false);
			Thread t = new Thread(r);
			t.start();
			try{
				t.join();
			}
			catch (Exception e)
			{
			}
			return r.getResponse().substring(1, 37);
		} catch (JSONException e1) {
		}
		
		return "";
	}	
}
