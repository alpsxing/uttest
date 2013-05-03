package com.ut50.ppk;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.Window;

public class NewActivityFragmentActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean titled = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.activity_new_activity);
		
		m_type = this.getIntent().getExtras().getString("type");
		
		if(titled){
        	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.new_activity_title_layout);
        }
		
		FragmentManager fragmentManager = this.getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		FragmentNewActivity newActivity = new FragmentNewActivity();
		newActivity.SetType(m_type);
		fragmentTransaction.add(R.id.new_activity_fragment_container, newActivity);
		fragmentTransaction.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_new_activity, menu);
		return true;
	}
	
	String m_type;

}
