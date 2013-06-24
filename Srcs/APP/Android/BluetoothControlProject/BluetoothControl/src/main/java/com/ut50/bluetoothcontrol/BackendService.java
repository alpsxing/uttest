package com.ut50.bluetoothcontrol;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by xingqq on 13-6-24.
 */
public class BackendService {
    private static final String TAG = "BackendService";
    private static final boolean D = true;

    private String mUrl;
    private float mLng;
    private float mLat;
    private String mDevice;
    private float mDir;
    private float mRadius;
    private float mAngle;

    public void setUrl(String url) {
        mUrl = url;
    }

    public void updateLoc(float lng, float lat) {
        mLng = lng;
        mLat = lat;
    }

    private class ReportThread extends Thread {
        private boolean mExit;

        public ReportThread() {
            mExit = false;
        }

        public void run() {
            Log.i(TAG, "BEGIN ReportThread:");
            setName("ReportThread");

            while(mExit == false) {
                HttpClient httpclient = new DefaultHttpClient();;
                String request_url = "http://" + mUrl + "/demo/btcom.php?";
                request_url += "device=";
                request_url += mDevice;
                request_url += "&lng=";
                request_url += Float.toString(mLng);
                request_url += "&lat=";
                request_url += Float.toString(mLat);
                request_url += "&d=";
                request_url += Float.toString(mDir);
                request_url += "&r=";
                request_url += Float.toString(mRadius);
                request_url += "&a=";
                request_url += Float.toString(mAngle);
                HttpGet httpget = new HttpGet(request_url);
                try {
                    HttpResponse response = httpclient.execute(httpget);
                    StatusLine statusLine = response.getStatusLine();
                    int statusCode = statusLine.getStatusCode();
                    if(statusCode == 200)
                    {
                        ;
                    }
                    else
                    {
                        Log.e(TAG, "Failed to get:" + statusLine.toString());
                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        public void cancel() {
            mExit = true;
        }
    }
}
