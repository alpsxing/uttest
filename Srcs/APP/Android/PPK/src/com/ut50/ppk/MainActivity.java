package com.ut50.ppk;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
	private ImageView imageView;
	private EditText edit_username;
	private EditText edit_password;
	private Button button_login;
	private Boolean animation_flag;
	private Boolean skip_animation;
	private ProgressDialog pd;
	private TextView text_prompt;
	private TextView text_create;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		imageView = (ImageView) findViewById(R.id.imageView_login);
		edit_username = (EditText) findViewById(R.id.edittext_username);
		edit_password = (EditText) findViewById(R.id.edittext_password);
		button_login = (Button) findViewById(R.id.button_login);
		text_prompt = (TextView) findViewById(R.id.textView_prompt);
		text_create = (TextView) findViewById(R.id.textView_create);
		
		edit_username.setOnClickListener(clickListener);
		edit_username.addTextChangedListener(textWatcher);
		edit_username.setOnFocusChangeListener(focusChangeListener);
		edit_password.setOnClickListener(clickListener);
		edit_password.addTextChangedListener(textWatcher);
		makeAllInVisible();
		animation_flag = false;
		skip_animation = false;
		
		String username = PreferenceManager.loadUsername(this);
		String password = PreferenceManager.loadPassword(this);
		
		Log.i(MainActivity.class.toString(), username + " " + password);
		
		if((username.trim().length() > 0) && (password.trim().length() > 0))
		{
			skip_animation = true;
		}
		
		edit_username.setText(username);
		edit_password.setText(password);
		isComplete();
		makeCreateClickable();
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        if(skip_animation == true)
        {
        	imageView.setVisibility(View.VISIBLE);
        	button_login_clicked(button_login);
        }
        else if(animation_flag == false)
        {
	        imageView.setVisibility(View.VISIBLE);
			TranslateAnimation translateAnim = (TranslateAnimation) AnimationUtils.loadAnimation(this, R.animator.logoanimator);
			translateAnim.setFillAfter(true);
			translateAnim.setAnimationListener(animationListener);
			imageView.startAnimation(translateAnim);
			animation_flag = true;
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
    
	/** Called when the user clicks the Login button */
	public void button_login_clicked(View view)
	{
		saveInfo(false);
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
    		pd = new ProgressDialog(this);
    		pd.setCancelable(false);
    		pd.show();
    		new LoginTask().execute();
        } else {
        	text_prompt.setText(R.string.prompt_no_network);
        }
	}
	
	private void makeAllVisible()
	{
		imageView.setVisibility(View.VISIBLE);
		edit_username.setVisibility(View.VISIBLE);
		edit_password.setVisibility(View.VISIBLE);
		button_login.setVisibility(View.VISIBLE);
		text_create.setVisibility(View.VISIBLE);
	}

	private void makeAllInVisible()
	{
		imageView.setVisibility(View.INVISIBLE);
		edit_username.setVisibility(View.INVISIBLE);
		edit_password.setVisibility(View.INVISIBLE);
		button_login.setVisibility(View.INVISIBLE);
		text_create.setVisibility(View.INVISIBLE);
	}
	
	private void isComplete()
	{
		if((edit_username.getText().toString().trim().length() > 0) && isUsernameEmptyOrValid() && (edit_password.getText().toString().trim().length() > 0))
		{
			button_login.setEnabled(true);
		}
		else
		{
			button_login.setEnabled(false);
		}
	}
	
	public void gotoHome()
	{
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		//Set the transition -> method available from Android 2.0 and beyond  
		overridePendingTransition(R.animator.push_left_in,R.animator.push_up_out);
		finish();
	}
	
	private Integer doLogin()
	{
		Integer ret = BackendHelper.RequestLogin(this, edit_username.getText().toString(), edit_password.getText().toString());
		if(ret != 0)
		{
			return ret;
		}
		
		saveInfo(true);
		return 0;
	}

	private void saveInfo(boolean password)
	{
		PreferenceManager.saveUsername(this, edit_username.getText().toString());
		
		if(password)
		{
			PreferenceManager.savePassword(this, edit_password.getText().toString());
		}
	}
	
	private void makeCreateClickable()
	{
		MovementMethod m = text_create.getMovementMethod();
		if ((m == null) || !(m instanceof LinkMovementMethod))
		{
			text_create.setMovementMethod(LinkMovementMethod.getInstance());
		}
		
		CharSequence text = text_create.getText();
		SpannableStringBuilder ssb = new SpannableStringBuilder(text);
		ssb.setSpan(new ForegroundColorSpan(0xFFFFFFFF), 0, text.length(), 0);
		ssb.setSpan(new UnderlineSpan(), 0, text.length(), 0);
		ssb.setSpan(clickableSpan, 0, text.length(), 0);
		text_create.setText(ssb);
		return;
	}
	
	private boolean isUsernameEmptyOrValid()
	{
		if(edit_username.getText().toString().trim().length() <= 0)
		{
			return true;
		}
		
		return edit_username.getText().toString().matches("[a-zA-Z0-9._-]+@[a-zA-Z0-9]+\\.([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+");
	}

	AnimationListener animationListener
	 = new AnimationListener(){

		@Override
		public void onAnimationEnd(Animation animation) {
			makeAllVisible();
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
		  
		}
		
		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
		  
		}
	};
	
	View.OnClickListener clickListener = new View.OnClickListener() {
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
	
	ClickableSpan clickableSpan = new ClickableSpan() {
		@Override
		public void onClick(View widget)
		{
	        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	        if (networkInfo != null && networkInfo.isConnected()) {
			    DialogFragment newFragment = new RegisterDialogFragment();
			    newFragment.show(getSupportFragmentManager(), "register");
	        } else {
	        	text_prompt.setText(R.string.prompt_no_network);
	        }
		}
	};
	
	private class LoginTask extends AsyncTask<Void, Void, Integer> {
		    /** The system calls this to perform work in a worker thread and
	      * delivers it the parameters given to AsyncTask.execute() */
	    protected Integer doInBackground(Void... paras) {
	    	return doLogin();
	    }
	    
	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
	    protected void onPostExecute(Integer result) {
	    	if(result != 0)
	    	{
	    		text_prompt.setText(result);
	    		pd.cancel();
	    		edit_password.setText("");
	    		makeAllVisible();
	    	}
	    	else
	    	{
	    		gotoHome();
	    	}
	    }
	}
}


