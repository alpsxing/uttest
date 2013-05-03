package com.ut50.ppk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PPKItemTimeView extends LinearLayout {

    public PPKItemTimeView(Context context) {
        super(context);
        SetupView(context);
    }

    public PPKItemTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SetupView(context);
    }
    
    public void SetImage(int resId)
    {
    	mImage.setImageResource(resId);
    }
    
    public void SetText(int resId)
    {
    	mTxt.setText(resId);
    }

    public void SetHint(int resId)
    {
    	mTxt.setHint(resId);
    }
    
    public void HideArrow()
    {
    	mArrow.setVisibility(View.INVISIBLE);
    }

    private void SetupView(Context context)
    {
        this.setOrientation(HORIZONTAL);

        // Here we build the child views in code. They could also have
        // been specified in an XML file.

        CreateImage(context);
        CreateText(context);
        CreateArrow(context);
    }

    private void CreateImage(Context context)
    {
    	mImage = new ImageView(context);
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
    	addView(mImage, params);
    }

    private void CreateText(Context context)
    {
    	mTxt = new EditText(context);
    	mTxt.setSingleLine(false);
    	mTxt.setLines(3);
    	mTxt.setTextSize(15);
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 3);
    	addView(mTxt, params);
    }

    private void CreateArrow(Context context)
    {
    	mArrow = new ImageView(context);
    	mArrow.setImageResource(R.drawable.navigation_next_item);
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0.5f);
    	addView(mArrow, params);
    }

    private ImageView mImage;
    private EditText mTxt;
    private ImageView mArrow;
}
