package com.ut50.ppk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

public class HomeActivity extends FragmentActivity
						  implements PPKTabView.TabClickedListener {

	private static final int NUM_PAGES = 3;

	private PPKTabView m_ppkTabView;
	private ViewPager m_viewPager;
	private PagerAdapter m_PagerAdapter;
	private LinearLayout m_typeLayout;
	private ScrollView m_scrollTypeChoose;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		boolean titled = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		setContentView(R.layout.activity_home);
		m_ppkTabView = (PPKTabView)this.findViewById(R.id.ppk_tab_view);
		m_viewPager = (ViewPager)this.findViewById(R.id.home_pager);
		m_PagerAdapter = new ScreenSlidePagerAdapter(this.getSupportFragmentManager());
		m_viewPager.setAdapter(m_PagerAdapter);
		m_viewPager.setCurrentItem(0);
		m_viewPager.setOnPageChangeListener(m_ppkTabView);
		
		PrepreButtons();

		if(titled){
        	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.home_title_layout);
    		ImageView newActivity = (ImageView)this.findViewById(R.id.imageView_home_title_new);
    		newActivity.setOnClickListener(newActivityClickListener);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_home, menu);
		return true;
	}
	
	View.OnClickListener newActivityClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v)
		{
			//ActivityTypeChooseDialogFragment newFragment = new ActivityTypeChooseDialogFragment();
		    //newFragment.show(getSupportFragmentManager(), "ChooseType");
			if(m_scrollTypeChoose.getVisibility() == View.VISIBLE)
			{
				HideTypeChooser();
			}
			else
			{
				PopupTypeChooser();
			}
		}
	};

	@Override
	public void onTabClicked(int position)
	{
		Log.i("HomeActivity::onTabClicked", "Position:" + position);
		m_viewPager.setCurrentItem(position);
	}
	
	View.OnClickListener typeClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v)
		{
			Log.i("HomeActivity::typeClickListener", "Clicked");
			HideTypeChooser();
			TextView txtView = (TextView) v;
			GotoNewActivity(txtView.getText().toString());
		}
	};
	
	private void GotoNewActivity(String text)
	{
		Intent intent = new Intent(this, NewActivityFragmentActivity.class);
		intent.putExtra("type", text);
		startActivity(intent);
		//Set the transition -> method available from Android 2.0 and beyond  
		overridePendingTransition(R.animator.push_left_in,R.animator.push_up_out);
	}

	private void PrepreButtons()
	{
		m_typeLayout = (LinearLayout)this.findViewById(R.id.layout_choose_type);
		m_scrollTypeChoose = (ScrollView)this.findViewById(R.id.scroll_choose_type);
		String[] actTypes = this.getResources().getStringArray(R.array.ActType);
		for(int i = 0; i < actTypes.length; i ++)
		{
			if(i > 0)
			{
				ImageView imgView = new ImageView(this);
				imgView.setImageResource(R.drawable.black_line);
				imgView.setScaleType(ScaleType.FIT_XY);
				LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0);
				imgParams.setMargins(5, 0, 5, 0);
				m_typeLayout.addView(imgView, imgParams);
			}

			TextView txtView = new TextView(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0);
			params.setMargins(5, 0, 5, 0);
			txtView.setText(actTypes[i]);
	        txtView.setGravity(Gravity.CENTER);
	        txtView.setPadding(0, 15, 0, 15);
	        txtView.setTextSize(20);
	        txtView.setTextColor(this.getResources().getColor(R.color.dropdown_text_color));
	        txtView.setOnClickListener(typeClickListener);
	        m_typeLayout.addView(txtView, params);
		}
		
		HideTypeChooser();
	}
	
	private void PopupTypeChooser()
	{
		m_scrollTypeChoose.setVisibility(View.VISIBLE);
	}

	private void HideTypeChooser()
	{
		m_scrollTypeChoose.setVisibility(View.INVISIBLE);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	if(m_scrollTypeChoose.getVisibility() == View.VISIBLE)
	    	{
	    		HideTypeChooser();
	    		return true;
	    	}
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	Fragment frg;
        	if(position == 0)
        	{
        		frg = new MyActivityPageFragment();
        	}
        	else if(position == 1)
        	{
        		frg = new ActivityCenterPageFragment();
        	}
        	else
        	{
        		frg = new UserCenterPageFragment();
        	}
        	return frg;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
