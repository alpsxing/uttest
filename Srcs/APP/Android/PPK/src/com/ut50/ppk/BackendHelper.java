package com.ut50.ppk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class BackendHelper {

	private static String http_api_host = "http://ppcore.sinaapp.com";
	private static String http_api_medium = "/api/v1";
    private static String http_field_front_type = "front_type";
    private static String http_value_front_type_app = "APP";
    private static String http_field_product_modal = "product_modal";
    private static String http_field_product_sn = "product_sn";
    private static String http_field_screen_width = "screen_width";
    private static String http_field_screen_height = "screen_height";
    private static String http_field_username = "uuid_mphone_or_email";
    private static String http_field_password = "encoded_pwd";
    private static String http_field_uuid = "uuid";
    private static String http_field_access_token = "access_token";
    private static String http_field_email = "email";
    private static String http_field_nickname = "nick_name";
    
    private static String http_response_field_resp_status = "resp_status";
    private static String http_response_field_error_desc = "error_desc";
    private static String http_response_field_front_id = "resp_data@front_id";
    private static String http_response_field_uuid = "resp_data@uuid";
    private static String http_response_field_access_token = "resp_data@access_token";
    private static String http_response_field_nick_name = "resp_data@nick_name";
    
    private static String http_response_field_type_string = "@string";
    
	public static Integer RequestFrontId(Context ctx, boolean force)
	{
		TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Integer height = display.getHeight();
		Integer width = display.getWidth();
		List<NameValuePair> postParas = new ArrayList<NameValuePair>(5);
		HashMap<String, StringBuilder> response = new HashMap<String, StringBuilder>();
		StringBuilder responseString = new StringBuilder();
		
		String front_id = PreferenceManager.loadFrontId(ctx);
		if((front_id != null) && (front_id.trim().length() > 0) && (force == false))
		{
			return 0;
		}
		
		PreferenceManager.saveFrontId(ctx, "");
		
		String urlRequest = http_api_host + http_api_medium;
		urlRequest += "/tool/?api_argus=gen_front_id/";

		postParas.add(new BasicNameValuePair(http_field_front_type, http_value_front_type_app));
		postParas.add(new BasicNameValuePair(http_field_product_modal, Build.MODEL));
		postParas.add(new BasicNameValuePair(http_field_product_sn, tm.getDeviceId()));
		postParas.add(new BasicNameValuePair(http_field_screen_width, width.toString()));
		postParas.add(new BasicNameValuePair(http_field_screen_height, height.toString()));
		
		PackReponse(response);
		response.put(http_response_field_front_id, new StringBuilder(http_response_field_type_string));
        
		if(SendPostRequest(urlRequest, postParas, responseString) == false)
		{
			return R.string.error_conn_fail;
		}
		
		if(ParseResponse(responseString.toString(), response) == false)
		{
			return R.string.error_login_fail;
		}
		
		if(isResponseOK(response) == false)
		{
			return R.string.error_login_fail;
		}
		
		front_id = fetchResult(response, http_response_field_front_id);
		
		if(front_id.length() <= 0)
		{
			return R.string.error_login_fail;
		}
		
		PreferenceManager.saveFrontId(ctx, front_id);
		
		return 0;
	}

	public static Integer RequestLogin(Context ctx, String username, String password)
	{
		List<NameValuePair> postParas = new ArrayList<NameValuePair>(2);
		HashMap<String, StringBuilder> response = new HashMap<String, StringBuilder>();
		StringBuilder responseString = new StringBuilder();
		
		Integer ret = RequestFrontId(ctx, false);
		if(ret != 0)
		{
			return ret;
		}
		
		String urlRequest = getSessionUrlPrefix(ctx);
		urlRequest += "/login/";

		postParas.add(new BasicNameValuePair(http_field_username, username));
		postParas.add(new BasicNameValuePair(http_field_password, password));
		
		PackReponse(response);
		response.put(http_response_field_error_desc, new StringBuilder(http_response_field_type_string));
		response.put(http_response_field_uuid, new StringBuilder(http_response_field_type_string));
		response.put(http_response_field_access_token, new StringBuilder(http_response_field_type_string));
		response.put(http_response_field_nick_name, new StringBuilder(http_response_field_type_string));
        
		if(SendPostRequest(urlRequest, postParas, responseString) == false)
		{
			return R.string.error_login_fail;
		}
		
		if(ParseResponse(responseString.toString(), response) == false)
		{
			return R.string.error_login_fail;
		}
		
		if(isResponseOK(response) == false)
		{
			if(response.containsKey(http_response_field_error_desc))
			{
				if(response.get(http_response_field_error_desc).toString().startsWith("Permission denied"))
				{
					return R.string.error_password_fail;
				}
			}
			return R.string.error_login_fail;
		}

		String nick_name = fetchResult(response, http_response_field_nick_name);
		if(nick_name.length() <= 0)
		{
			return R.string.error_login_fail;
		}
		PreferenceManager.saveNickname(ctx, nick_name);

		String uuid = fetchResult(response, http_response_field_uuid);
		if(uuid.length() <= 0)
		{
			return R.string.error_login_fail;
		}
		PreferenceManager.saveUuid(ctx, uuid);
		
		String access_token = fetchResult(response, http_response_field_access_token);
		if(access_token.length() <= 0)
		{
			return R.string.error_login_fail;
		}
		PreferenceManager.saveAccessToken(ctx, access_token);

		return 0;
	}

	public static Integer RequestLogout(Context ctx)
	{
		List<NameValuePair> postParas = new ArrayList<NameValuePair>(2);
		HashMap<String, StringBuilder> response = new HashMap<String, StringBuilder>();
		StringBuilder responseString = new StringBuilder();
		
		String urlRequest = getSessionUrlPrefix(ctx);
		urlRequest += "/login/";

		String uuid = PreferenceManager.loadUuid(ctx);
		String access_token = PreferenceManager.loadAccessToken(ctx);
		
		postParas.add(new BasicNameValuePair(http_field_uuid, uuid));
		postParas.add(new BasicNameValuePair(http_field_access_token, access_token));
		
		PackReponse(response);
		
		uuid = "";
		access_token = "";
		PreferenceManager.saveUuid(ctx, uuid);
		PreferenceManager.saveAccessToken(ctx, access_token);
        
		if(SendDeleteRequest(urlRequest, postParas, responseString) == false)
		{
			return R.string.error_logout_fail;
		}
		
		if(ParseResponse(responseString.toString(), response) == false)
		{
			return R.string.error_logout_fail;
		}
		
		if(isResponseOK(response) == false)
		{
			return R.string.error_logout_fail;
		}
		
		return 0;
	}
	
	public static Integer RequestRegister(Context ctx, String username, String password, String nickname)
	{
		List<NameValuePair> postParas = new ArrayList<NameValuePair>(3);
		HashMap<String, StringBuilder> response = new HashMap<String, StringBuilder>();
		StringBuilder responseString = new StringBuilder();
		
		Integer ret = RequestFrontId(ctx, false);
		if(ret != 0)
		{
			return ret;
		}
		
		String urlRequest = getSessionUrlPrefix(ctx);
		urlRequest += "/register/";

		postParas.add(new BasicNameValuePair(http_field_email, username));
		postParas.add(new BasicNameValuePair(http_field_password, password));
		postParas.add(new BasicNameValuePair(http_field_nickname, nickname));
		
		PackReponse(response);
		response.put(http_response_field_error_desc, new StringBuilder(http_response_field_type_string));
		response.put(http_response_field_uuid, new StringBuilder(http_response_field_type_string));
		response.put(http_response_field_access_token, new StringBuilder(http_response_field_type_string));
		response.put(http_response_field_nick_name, new StringBuilder(http_response_field_type_string));
		
		if(SendPostRequest(urlRequest, postParas, responseString) == false)
		{
			return R.string.error_register_fail;
		}
		
		if(ParseResponse(responseString.toString(), response) == false)
		{
			return R.string.error_register_fail;
		}
		
		if(isResponseOK(response) == false)
		{
			if(response.containsKey(http_response_field_error_desc))
			{
				if(response.get(http_response_field_error_desc).toString().startsWith("The email"))
				{
					return R.string.error_register_username_used;
				}
			}
			return R.string.error_register_fail;
		}

		String nick_name = fetchResult(response, http_response_field_nick_name);
		if(nick_name.length() <= 0)
		{
			return R.string.error_register_fail;
		}
		PreferenceManager.saveNickname(ctx, nick_name);

		String uuid = fetchResult(response, http_response_field_uuid);
		if(uuid.length() <= 0)
		{
			return R.string.error_register_fail;
		}
		PreferenceManager.saveUuid(ctx, uuid);
		
		String access_token = fetchResult(response, http_response_field_access_token);
		if(access_token.length() <= 0)
		{
			return R.string.error_register_fail;
		}
		PreferenceManager.saveAccessToken(ctx, access_token);

		return 0;
	}
	
	private static boolean SendPostRequest(String urlRequest, List<NameValuePair> postParas, StringBuilder responseString)
	{
		InputStream content = null;
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(urlRequest);

		Log.i(BackendHelper.class.toString(), urlRequest);

	    try {
	        // Add your data
	        httppost.setEntity(new UrlEncodedFormEntity(postParas));
	        
	        Log.i(BackendHelper.class.toString(), httppost.toString());

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        StatusLine statusLine = response.getStatusLine();
	        int statusCode = statusLine.getStatusCode();
	        if(statusCode == 200)
	        {
	            HttpEntity entity = response.getEntity();
	            content = entity.getContent();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	            String line;
	            while ((line = reader.readLine()) != null) {
	            	responseString.append(line);
	            }
	            if(responseString.toString().trim().length() <= 0)
	            {
	            	return false;
	            }
	        }
	        else
	        {
	        	Log.e(BackendHelper.class.toString(), "Failed to post:" + statusLine.toString());
	        	return false;
	        }
	    } catch (ClientProtocolException e) {
	        e.printStackTrace();
        	return false;
	    } catch (UnsupportedEncodingException e)
	    {
	    	e.printStackTrace();
        	return false;
	    } catch (IOException e)
	    {
	    	e.printStackTrace();
        	return false;
	    }
	    
        return true;
	}

	private static boolean SendDeleteRequest(String urlRequest, List<NameValuePair> deleteParas, StringBuilder responseString)
	{
		InputStream content = null;
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpDelete httpdelete = new HttpDelete(urlRequest);

		Log.i(BackendHelper.class.toString(), urlRequest);

	    try {
	        // Add your data
	    	Iterator<NameValuePair> it = deleteParas.iterator();
	    	while(it.hasNext())
	    	{
	    		NameValuePair pair = it.next();
	    		httpdelete.addHeader(pair.getName(), pair.getValue());
	    	}
	        
	        Log.i(BackendHelper.class.toString(), httpdelete.toString());

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httpdelete);
	        StatusLine statusLine = response.getStatusLine();
	        int statusCode = statusLine.getStatusCode();
	        if(statusCode == 200)
	        {
	            HttpEntity entity = response.getEntity();
	            content = entity.getContent();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	            String line;
	            while ((line = reader.readLine()) != null) {
	            	responseString.append(line);
	            }
	            if(responseString.toString().trim().length() <= 0)
	            {
	            	return false;
	            }
	        }
	        else
	        {
	        	Log.e(BackendHelper.class.toString(), "Failed to delete:" + statusLine.toString());
	        	return false;
	        }
	    } catch (ClientProtocolException e) {
	        e.printStackTrace();
        	return false;
	    } catch (UnsupportedEncodingException e)
	    {
	    	e.printStackTrace();
        	return false;
	    } catch (IOException e)
	    {
	    	e.printStackTrace();
        	return false;
	    }
	    
        return true;
	}
	
	private static boolean ParseResponse(String responseString, HashMap<String, StringBuilder> response)
	{
		JSONObject jsonObject;
		
        Log.i(BackendHelper.class.toString(), responseString);
        try {
        	jsonObject = new JSONObject(responseString);
        } catch (Exception e) {
	        e.printStackTrace();
	        return false;
        }
        
        Iterator<String> it = response.keySet().iterator();
        while(it.hasNext())
        {
        	String name = it.next();
        	String[] tokens = name.split("@");
        	StringBuilder value = response.get(name);
        	if(FindResponse(jsonObject, tokens, 0, value) == false)
        	{
        		value.setLength(0);
        	}
        }

        return true;
	}
	
	private static boolean FindResponse(JSONObject jsonObject, String[] tokens, int depth, StringBuilder value)
	{
	
		if(jsonObject.isNull(tokens[depth]))
		{
			return false;
		}
		
		if(tokens.length <= (depth + 1))
		{
			try
			{
				if(value.toString().equals(http_response_field_type_string))
				{
					value.setLength(0);
					value.append(jsonObject.getString(tokens[depth]));
					return true;
				}
			} catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				JSONObject nextJson = jsonObject.getJSONObject(tokens[depth]);
				return FindResponse(nextJson, tokens, depth + 1, value);
			} catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
	
		return false;
	}
	
	private static void PackReponse(HashMap<String, StringBuilder> response)
	{
		response.put(http_response_field_resp_status, new StringBuilder(http_response_field_type_string));
	}
	
	private static boolean isResponseOK(HashMap<String, StringBuilder> response)
	{
		if(response.containsKey(http_response_field_resp_status))
		{
			if(response.get(http_response_field_resp_status).toString().equals("OK"))
			{
				return true;
			}
		}
		return false;
	}
	
	private static String fetchResult(HashMap<String, StringBuilder> response, String key)
	{
		if(response.containsKey(key))
		{
			return response.get(key).toString();
		}
		return "";
	}
	
	private static String getSessionUrlPrefix(Context ctx)
	{
		String urlRequest = http_api_host + http_api_medium;
		urlRequest += "/front/?api_argus=";
		
		String front_id = PreferenceManager.loadFrontId(ctx);
		
		urlRequest += front_id;
		return urlRequest;
	}

}

