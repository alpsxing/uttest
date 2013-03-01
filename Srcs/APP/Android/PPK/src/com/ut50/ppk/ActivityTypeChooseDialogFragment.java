package com.ut50.ppk;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ActivityTypeChooseDialogFragment extends DialogFragment {
	private ListView m_listView_typeChooser;
	private ActivityTypeChooseDialogListener m_listener;
	
    public interface ActivityTypeChooseDialogListener {
        public void onListResult(int result);
    }
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.dialog_choose_activity_type, container, false);
    	m_listView_typeChooser = (ListView)v.findViewById(R.id.listview_typechooser);
    	
    	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(v.getContext(),
    	        R.array.ActType, android.R.layout.simple_list_item_1);
    	m_listView_typeChooser.setAdapter(adapter);
    	
    	m_listView_typeChooser.setOnItemClickListener(clickListener);
        return v;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.setTitle(R.string.title_choose_activity_type);
        dialog.getWindow().setGravity(Gravity.END | Gravity.TOP);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            m_listener = (ActivityTypeChooseDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ActivityTypeChooseDialogListener");
        }
    }

	AdapterView.OnItemClickListener	clickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			m_listener.onListResult(position);
			getDialog().cancel();
			return;
		}
	};
}
