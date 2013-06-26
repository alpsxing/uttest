package com.ut50.bluetoothcontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
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

    private Context mContext;
    private String mUrl;
    private double mLng;
    private double mLat;
    private String mDevice;
    private float mDir;
    private float mRadius;
    private float mAngle;
    private Object mLock;

    private ReportThread mReportThread;

    public BackendService(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        mDevice = tm.getDeviceId();
        mContext = ctx;
        mReportThread = null;
        mLock = new Object();
        update();
        mLng = 0.0f;
        mLat = 0.0f;
        mDir = 0.0f;
    }

    public synchronized void Start() {
        if (D) Log.d(TAG, "start");

        if (mReportThread != null) {
            mReportThread.cancel();
            try {
                mReportThread.join();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            mReportThread = null;
        }

        mReportThread = new ReportThread(this);
        mReportThread.start();
    }

    public synchronized void Stop() {
        if (D) Log.d(TAG, "stop");

        if (mReportThread != null) {
            mReportThread.cancel();
            try {
                mReportThread.join();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            mReportThread = null;
        }
    }

    public void update() {
        synchronized (mLock) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
            mUrl = sharedPref.getString(mContext.getString(R.string.key_url), mContext.getString(R.string.default_value_url));

            String radius = sharedPref.getString(mContext.getString(R.string.key_radius), mContext.getString(R.string.default_value_radius));
            mRadius = Float.parseFloat(radius);
            if(mRadius <= 0.0f) {
                mRadius = Float.parseFloat(mContext.getString(R.string.default_value_radius));
            }

            String angle = sharedPref.getString(mContext.getString(R.string.key_angle), mContext.getString(R.string.default_value_angle));
            mAngle = Float.parseFloat(angle);
            if(mAngle <= 0.0f) {
                mAngle = Float.parseFloat(mContext.getString(R.string.default_value_angle));
            }
        }
    }

    public void updateLoc(double lng, double lat) {
        synchronized (mLock) {
            mLng = lng;
            mLat = lat;
        }
    }

    public void updateDir(float dir) {
        synchronized (mLock) {
            mDir = dir;
        }
    }

    private class ReportThread extends Thread {
        BackendService mBackendService;
        private boolean mExit;

        public ReportThread(BackendService backend) {
            mExit = false;
            mBackendService = backend;
        }

        public void run() {
            Log.i(TAG, "BEGIN ReportThread:");
            setName("ReportThread");

            while(mExit == false) {
                HttpClient httpclient = new DefaultHttpClient();
                String request_url = "http://";
                synchronized (mLock) {
                    request_url += mUrl + "/demo/btcom.php?";
                    request_url += "device=";
                    request_url += mDevice;
                    request_url += "&lng=";
                    request_url += Double.toString(mLng);
                    request_url += "&lat=";
                    request_url += Double.toString(mLat);
                    request_url += "&d=";
                    request_url += Float.toString(mDir);
                    request_url += "&r=";
                    request_url += Float.toString(mRadius);
                    request_url += "&a=";
                    request_url += Float.toString(mAngle);
                }

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

                try {
                    Thread.sleep(500);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void cancel() {
            mExit = true;
        }
    }
}
