package com.ut50.ppk;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class HomeActivity extends FragmentActivity
						  implements ActivityTypeChooseDialogFragment.ActivityTypeChooseDialogListener, 
						  PPKTabView.TabClickedListener {

	private static final int NUM_PAGES = 3;

	PPKTabView m_ppkTabView;
	ViewPager m_viewPager;
	private PagerAdapter m_PagerAdapter;
	RelativeLayout.LayoutParams savePreviousLayoutParams;
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


		if(titled){
        	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.home_title_layout);
        }
		
		ImageView previousArrow = (ImageView)this.findViewById(R.id.imageView_previous);
		savePreviousLayoutParams = (RelativeLayout.LayoutParams)previousArrow.getLayoutParams();
		previousArrow.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
		
		ImageView newActivity = (ImageView)this.findViewById(R.id.imageView_title_new);
		newActivity.setOnClickListener(clickListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_home, menu);
		return true;
	}
	
	View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v)
		{
			ActivityTypeChooseDialogFragment newFragment = new ActivityTypeChooseDialogFragment();
		    newFragment.show(getSupportFragmentManager(), "ChooseType");
		}
	};

	@Override
	public void onListResult(int result)
	{
		Log.i("HomeActivity::onListResult", "Result:" + result);
	}

	@Override
	public void onTabClicked(int position)
	{
		Log.i("HomeActivity::onTabClicked", "Position:" + position);
		m_viewPager.setCurrentItem(position);
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
