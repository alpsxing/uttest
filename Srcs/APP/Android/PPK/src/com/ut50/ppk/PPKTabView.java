package com.ut50.ppk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.view.ViewPager;

public class PPKTabView extends LinearLayout
						implements ViewPager.OnPageChangeListener {

	public interface TabClickedListener {
        public void onTabClicked(int position);
    }

    public PPKTabView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setOrientation(HORIZONTAL);

        // Here we build the child views in code. They could also have
        // been specified in an XML file.

        mMyAct = new TextView(context);
        mMyAct.setText(context.getString(R.string.text_my_activity));
        SetTextViewApperance(mMyAct, false, 0);
        mActCenter = new TextView(context);
        mActCenter.setText(context.getString(R.string.text_activity_center));
        SetTextViewApperance(mActCenter, false, 1);
        mUserCenter = new TextView(context);
        mUserCenter.setText(context.getString(R.string.text_user_center));
        SetTextViewApperance(mUserCenter, false, 2);

        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            m_listener = (TabClickedListener)context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement TabClickedListener");
        }
    }
    
    private void SetTextViewApperance(TextView txtView, boolean selected, int position) // 0£º leftmost 1: middle 2:rightmost
    {
        txtView.setGravity(Gravity.CENTER);
        txtView.setPadding(0, 25, 0, 25);
        ChangeTextViewAppearance(mMyAct, txtView);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        
        if(position == 0)
        {
        	params.setMargins(0, 0, 1, 0);
        }
        else if(position == 2)
        {
        	params.setMargins(1, 0, 0, 0);
        }
        else
        {
        	params.setMargins(1, 0, 1, 0);
        }
        addView(txtView, params);
        txtView.setOnClickListener(clickListener);
    }
    
    private void ChangeTextViewAppearance(TextView clickedView, TextView v)
    {
    	if(v == clickedView)
    	{
    		v.setBackgroundResource(R.drawable.tab_border_selected);
    		v.setTextColor(this.getResources().getColor(R.color.tab_text_selected_color));
    	}
    	else
    	{
    		v.setBackgroundResource(R.drawable.tab_border_normal);
    		v.setTextColor(this.getResources().getColor(R.color.tab_text_normal_color));
    	}
    }
    
	View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v)
		{
			ChangeTextViewAppearance((TextView)v, mMyAct);
			ChangeTextViewAppearance((TextView)v, mActCenter);
			ChangeTextViewAppearance((TextView)v, mUserCenter);
			TriggerTabClickEvent(v);
		}
	};
	
	private void TriggerTabClickEvent(View v)
	{
		int position;
		
		if(v == mMyAct)
		{
			position = 0;
		}
		else if(v == mActCenter)
		{
			position = 1;
		}
		else
		{
			position = 2;
		}
		
		m_listener.onTabClicked(position);
	}

	@Override
	public void onPageScrollStateChanged(int state)
	{
		;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
	{
		;
	}

	@Override
	public void onPageSelected(int position)
	{
		if(position == 0)
		{
			clickListener.onClick(mMyAct);
		}
		else if(position == 1)
		{
			clickListener.onClick(mActCenter);
		}
		else
		{
			clickListener.onClick(mUserCenter);
		}
	}

    private TextView mMyAct;
    private TextView mActCenter;
    private TextView mUserCenter;
    
    private TabClickedListener m_listener;
}
