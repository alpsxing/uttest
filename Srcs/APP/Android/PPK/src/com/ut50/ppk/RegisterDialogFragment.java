package com.ut50.ppk;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterDialogFragment extends DialogFragment {
	private ProgressDialog pd;
	private TextView text_prompt;
	private Button button_create;
	EditText edit_username;
	EditText edit_password;
	EditText edit_nickname;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.dialog_register, container, false);
    	button_create = (Button)v.findViewById(R.id.button_register);
    	Button button_cancel = (Button)v.findViewById(R.id.button_register_cancel);
    	text_prompt = (TextView)v.findViewById(R.id.textView_register_prompt);
    	edit_username = (EditText)v.findViewById(R.id.edittext_register_username);
    	edit_password = (EditText)v.findViewById(R.id.edittext_register_password);
    	edit_nickname = (EditText)v.findViewById(R.id.edittext_register_nickname);
    	edit_username.setOnClickListener(clickTextListener);
		edit_username.addTextChangedListener(textWatcher);
		edit_username.setOnFocusChangeListener(focusChangeListener);
    	edit_password.setOnClickListener(clickTextListener);
    	edit_password.addTextChangedListener(textWatcher);
    	edit_nickname.setOnClickListener(clickTextListener);
    	edit_nickname.addTextChangedListener(textWatcher);
    	button_create.setOnClickListener(clickRegisterListener);
    	button_cancel.setOnClickListener(clickCancelListener);
    	text_prompt.setText("");
    	isComplete();

        // Inflate the layout to use as dialog or embedded fragment
        return v;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.title_register);
        return dialog;
    }
    
	private void isComplete()
	{
		if((edit_username.getText().toString().trim().length() > 0) &&
				isUsernameEmptyOrValid() &&
				(edit_password.getText().toString().trim().length() > 0) &&
				(edit_nickname.getText().toString().trim().length() > 0))
		{
			button_create.setEnabled(true);
		}
		else
		{
			button_create.setEnabled(false);
		}
	}

    
	View.OnClickListener clickRegisterListener = new View.OnClickListener() {
		@Override
		public void onClick(View v)
		{
			doCreate();
		}
	};

	View.OnClickListener clickCancelListener = new View.OnClickListener() {
		@Override
		public void onClick(View v)
		{
			getDialog().cancel();
		}
	};

	View.OnClickListener clickTextListener = new View.OnClickListener() {
		@Override
		public void onClick(View v)
		{
			text_prompt.setText("");
		}
	};

	TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s)
		{
			isComplete();
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{
			;
		}
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			;
		}
	};
	
    private void doCreate()
    {
		pd = new ProgressDialog(getActivity());
		pd.setCancelable(false);
		pd.show();
		new RegisterTask().execute();
    }
    
    private Integer doRegister()
    {
    	EditText editText_username = (EditText)getDialog().findViewById(R.id.edittext_register_username);
    	EditText editText_password = (EditText)getDialog().findViewById(R.id.edittext_register_password);
    	EditText editText_nickname = (EditText)getDialog().findViewById(R.id.edittext_register_nickname);
    	return BackendHelper.RequestRegister(getActivity(), 
    				editText_username.getText().toString().trim(), 
    				editText_password.getText().toString().trim(), 
    				editText_nickname.getText().toString().trim());
    }
    
	private boolean isUsernameEmptyOrValid()
	{
		if(edit_username.getText().toString().trim().length() <= 0)
		{
			return true;
		}
		
		return edit_username.getText().toString().matches("[a-zA-Z0-9._-]+@[a-zA-Z0-9]+\\.([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+");
	}
	
	View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(!hasFocus)
			{
		        if (!isUsernameEmptyOrValid())
		        {
		        	edit_username.setError(getString(R.string.prompt_invalid_username));
		        }
		        else
		        {
		        	edit_username.setError(null);
		        }
			}
		}
	};
    
	private class RegisterTask extends AsyncTask<Void, Void, Integer> {
	    /** The system calls this to perform work in a worker thread and
      * delivers it the parameters given to AsyncTask.execute() */
	    protected Integer doInBackground(Void... paras) {
	    	return doRegister();
	    }
	    
	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
	    protected void onPostExecute(Integer result) {
	    	if(result != 0)
	    	{
	    		pd.cancel();
	    		text_prompt.setText(result);
	    	}
	    	else
	    	{
	    		MainActivity main = (MainActivity) getActivity();
	    		main.gotoEntry();
	    	}
	    }
	}
}
