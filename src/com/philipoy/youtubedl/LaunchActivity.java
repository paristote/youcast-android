package com.philipoy.youtubedl;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import retrofit.RetrofitError;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.philipoy.youtubedl.rest.YouCastInterface;
import com.philipoy.youtubedl.utils.CredentialsManager;

/**
 * 
 * @author Philippe Aristote
 *
 */
public class LaunchActivity extends Activity {
	
	public LaunchActivity() {
		super();
	}

	private LinearLayout signinArea;
	private EditText emailField;
	private EditText passField;
	private Button signinButton;
	private ProgressBar progress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());
		setContentView(R.layout.activity_launch);
		
		signinArea = (LinearLayout)findViewById(R.id.signin_container);
		signinArea.setVisibility(View.GONE);
		
		emailField = (EditText)findViewById(R.id.signin_email_field);
		passField = (EditText)findViewById(R.id.signin_password_field);
		signinButton = (Button)findViewById(R.id.signin_signin_btn);
		progress = (ProgressBar)findViewById(R.id.signin_progress);
		
		signinButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String email = emailField.getText().toString();
				String password = passField.getText().toString();
				if (!email.isEmpty() && !password.isEmpty()) {
					new LoginTask(LaunchActivity.this, email, password).execute();
				} else {
					Toast.makeText(LaunchActivity.this, R.string.toast_signin_failed, Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	private void startLoading() {
		signinButton.setVisibility(View.GONE);
		progress.setVisibility(View.VISIBLE);
	}
	
	private void stopLoading() {
		signinButton.setVisibility(View.VISIBLE);
		progress.setVisibility(View.GONE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (CredentialsManager.getInstance(this).credentialsExist()) {
			// immediately opens the main activity if the credentials already exist, i.e. the user is already signed-in
			openMainActivity();
		}
		else {
			// otherwise, display the sign-in form
			signinArea.setVisibility(View.VISIBLE);
		}
	}
	/**
	 * Open the MainActivity in a new cleared task.
	 * Then, finishes the activity.
	 * Therefore, users cannot navigate to this activity with the [back] button.
	 */
	public void openMainActivity() {
		Intent main = new Intent(this, MainActivity.class);
		main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(main);
		finish();
	}
	
	
	/**
	 * 
	 * @author Philippe Aristote
	 *
	 */
	private static class LoginTask extends AsyncTask<Void, Void, String>
	{
		private String email;
		private String password;
		private LaunchActivity activity;
		public LoginTask(LaunchActivity activity, String email, String password) {
			this.email = email;
			this.password = password;
			this.activity = activity;
		}
		
		@Override
		protected void onPreExecute() {
			activity.startLoading();
		}
		
		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			YouCastInterface yc = YouCastInterface.Factory.create(email, password);
			try {
				result = yc.connect(email);
			} catch (RetrofitError e) {
				Log.d(App.LOG_TAG, "[Error] "+e.getUrl()+" : "+e.getResponse().getStatus()+" - "+e.getResponse().getReason());
				result = "FAILED";
			}
			if ("OK".equals(result)) {
				CredentialsManager.getInstance(activity).storeCredentials(email, password);
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			activity.stopLoading();
			if ("OK".equalsIgnoreCase(result)) {
				Toast.makeText(activity, R.string.toast_signin_success, Toast.LENGTH_SHORT).show();
				activity.openMainActivity();
			} else {
				Toast.makeText(activity, R.string.toast_signin_failed, Toast.LENGTH_LONG).show();
			}
			activity = null;
		}
		
	}
}
