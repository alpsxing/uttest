package com.ut50.ppk;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;

public class EntryActivity extends MapActivity  {

	private ProgressDialog pd;
	BMapManager mBMapMan = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entry);
		
	    mBMapMan = new BMapManager(getApplication());
	    mBMapMan.init("E95B06CCA5D8292E99AEEBA916353247A52100E7", null);
	    super.initMapActivity(mBMapMan);
	     
	    MapView mMapView = (MapView) findViewById(R.id.bmapsView);
	    mMapView.setBuiltInZoomControls(true); //设置启用内置的缩放控件
	     
	    MapController mMapController = mMapView.getController(); // 得到mMapView的控制权,可以用它控制和驱动平移和缩放
	    GeoPoint point = new GeoPoint((int) (39.915 * 1E6),
	    (int) (116.404 * 1E6)); //用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
	    mMapController.setCenter(point); //设置地图中心点
	    mMapController.setZoom(12); //设置地图zoom级别
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_entry, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_logout:
	        	Logout();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void Logout()
	{
		PreferenceManager.savePassword(this, "");
		pd = new ProgressDialog(this);
		pd.setCancelable(false);
		pd.show();
		new LogoutTask().execute(this);
	}
	
	private void gotoMain()
	{
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
	

	private class LogoutTask extends AsyncTask<Context, Void, Integer> {
	    /** The system calls this to perform work in a worker thread and
	  * delivers it the parameters given to AsyncTask.execute() */
	    protected Integer doInBackground(Context... paras) {
			return BackendHelper.RequestLogout(paras[0]);
	    }

		/** The system calls this to perform work in the UI thread and delivers
		  * the result from doInBackground() */
		protected void onPostExecute(Integer result) {
			if(result != 0)
			{
				Log.e(LogoutTask.class.toString(), "Failed to logout " + result);
			}
			gotoMain();
		}
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
    @Override
    protected void onDestroy() {
    if (mBMapMan != null) {
    mBMapMan.destroy();
    mBMapMan = null;
    }
    super.onDestroy();
    }
    @Override
    protected void onPause() {
    if (mBMapMan != null) {
    mBMapMan.stop();
    }
    super.onPause();
    }
    @Override
    protected void onResume() {
    if (mBMapMan != null) {
    mBMapMan.start();
    }
    super.onResume();
    }
}
