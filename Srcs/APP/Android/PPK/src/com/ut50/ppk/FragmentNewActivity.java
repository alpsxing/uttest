package com.ut50.ppk;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FragmentNewActivity extends ListFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Integer List[] = {0, 1, 2};
        
        setListAdapter(new ArrayAdapter<Integer>(this.getActivity(), R.layout.fragment_new_activity, List) {
        	@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        		if(position == 0)
        		{
        			return CreateSubTitle(parent.getContext());
        		}
        		else if(position == 1)
        		{
        			return CreateActivityInfo(parent.getContext());
        		}
        		else if(position == 2)
        		{
        			return CreateActivityLocation(parent.getContext());
        		}
        		else
        		{
        			return new PPKItemView(this.getContext()); 
        		}
        	}
        	
        	private View CreateSubTitle(Context context)
        	{
        		m_subTitle = new TextView(context);
        		m_subTitle.setText(context.getString(R.string.text_create) + m_type);
        		m_subTitle.setPadding(25, 25, 0, 25);
        		m_subTitle.setBackgroundColor(context.getResources().getColor(R.color.subtitle_background_color));
        		m_subTitle.setTextColor(context.getResources().getColor(R.color.subtitle_text_color));

        		return m_subTitle;
        	}
        	
        	private View CreateActivityInfo(Context context)
        	{
        		PPKItemView view = new PPKItemView(this.getContext());
        		view.SetImage(R.drawable.content_picture);
        		view.SetHint(R.string.hint_set_activity_info);
        		view.setBackgroundResource(R.drawable.new_activity_item_border);
        		
        		return view;
        	}

        	private View CreateActivityLocation(Context context)
        	{
        		PPKItemView view = new PPKItemView(this.getContext());
        		view.SetImage(R.drawable.location_map);
        		view.SetHint(R.string.hint_set_location);
        		view.setBackgroundResource(R.drawable.new_activity_item_border);
        		view.HideArrow();
        		return view;
        	}
       	});
        
    }
    
    public void SetType(String type)
    {
    	m_type = type;
    }
    private TextView m_subTitle;
    private String m_type;
}
