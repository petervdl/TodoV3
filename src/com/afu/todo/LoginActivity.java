package com.afu.todo;

import java.io.IOException;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.Kii.Site;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.KiiUser.Builder;
import com.kii.cloud.storage.exception.app.AppException;
import com.kii.cloud.storage.exception.app.BadRequestException;
import com.kii.cloud.storage.exception.app.ConflictException;
import com.kii.cloud.storage.exception.app.ForbiddenException;
import com.kii.cloud.storage.exception.app.NotFoundException;
import com.kii.cloud.storage.exception.app.UnauthorizedException;
import com.kii.cloud.storage.exception.app.UndefinedException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.    There are 2 ways to login -
 *   1.  type name and passwd - press login, 
 *   2.  type name and passwd - press register - for a name that is new to Kii.
 *   
 * Updated my app to store credentials and dat in the Kii cloud
 *    Peter van der Linden, May 24 2014    
 */

final class Constants {   // you need to get these 2 numbers and fill them in below.
    static final String appid = "GET YOUR ID FROM developer.kii.com";
    static final String appkey = "GET YOUR APPKEY FROM developer.kii.com";
}


public class LoginActivity extends Activity {

	/**
	 * We don't use this, but could.
	 */
	public static String token = "E4UixTXlWD_UtpQ4M6jjKqRnbjoGRNl8yKpUGG4O7tg";


	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// local saved values for email and password used in login attempt.
	private static String mEmail;
	private static String mPassword;
	private static Context saved_ctxt;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		mEmailView = (EditText) findViewById(R.id.email);		
		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin(false);
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		
		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin( false );
					}
				});
		
		findViewById(R.id.reguser_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin( true );
					}
				});

	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		super.onCreateOptionsMenu(menu);
//		getMenuInflater().inflate(R.menu.login, menu);
//		return true;
//	}

	
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are logged/reported and no login attempt is made.
	 */
	public void attemptLogin(boolean registernew) {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask( registernew ); // register new user, or sign in existing user.
			saved_ctxt = this;
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	KiiUser kUser = null; // keeps a handle to current user.
	
	/**
	 * asynchronous login/registration task to authenticate
	 * the user against the Kii registration
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		private final boolean registering_new;
        private boolean fail_pass = false;
		
        public UserLoginTask(boolean param1) {
			registering_new = param1; //says whether existing login, or new registration
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// usernames must be (insert lexical rules here)
			// passwds must be (insert lexical rules here) & at least 4 chars
			// rules at: (insert URL here)
			Kii.initialize(Constants.appid, Constants.appkey, Site.US);
			String toastmsg = "";
			String accessToken = "";
			fail_pass = false;
			if (! registering_new) {
				// try login with these credentials.
				try {
					Log.e("todo", "login existing user");
					KiiUser user = KiiUser.logIn(mEmail, mPassword);
					kUser = user;
					Log.e("todo", "no exceptions on login existing user - OK");
					accessToken = user.getAccessToken();  // we don't use this, but could save to preference

				} catch (BadRequestException e) {
					toastmsg = "Excpn on login - user or password doesn't match";
					e.printStackTrace();
				} catch (UnauthorizedException e) {
					toastmsg="Excpn on login - unauthorized";
					e.printStackTrace();
				} catch (ForbiddenException e) {
					toastmsg = "Excpn on login - forbidden";
					e.printStackTrace();
				} catch (ConflictException e) {
					toastmsg = "Excpn on login - conflict";
					e.printStackTrace();
				} catch (NotFoundException e) {
					toastmsg = "Excpn on login - not found";
					e.printStackTrace();
				} catch (UndefinedException e) {
					toastmsg = "Excpn on login - undefined";
					e.printStackTrace();
				} catch (IOException e) {
					toastmsg = "Excpn on login - IO Exception";
					e.printStackTrace();
				} catch (Exception e) {
					toastmsg = "some other Excpn on login " + e.getMessage() + "- check logcat";
					e.printStackTrace();
				} finally { fail_pass = (toastmsg == "" );  // no message == all OK

				}
			} else { // we are registering a new user
				try {
					Log.e("todo", "register new user");
					Builder builder = KiiUser.builderWithName(mEmail);
					KiiUser user = builder.build();
					user.register(mPassword);
				} catch (AppException e) {
					toastmsg = "App Excpn on login " + e.getMessage() + "- check logcat";
					e.printStackTrace();
				} catch (IOException e) {
					toastmsg = "IO Excpn on login " + e.getMessage() + "- check logcat";
					e.printStackTrace();
				} catch (Exception e) {
					toastmsg = "some other Excpn on login " + e.getMessage() + "- check logcat";
					e.printStackTrace();
				}  finally {
					fail_pass = (toastmsg == ""); // no message == all OK
                   }
			}
			if (!fail_pass) {
				final String msg = toastmsg;
				((Activity) saved_ctxt).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(saved_ctxt, msg, Toast.LENGTH_LONG).show();
					}
				});	            
			}
			return fail_pass;  // false = login/register failed, true = logged/registered OK.
		}

		
		@Override
		protected void onPostExecute(final Boolean goodLogin) {
			// test data - peter/peter
			// pvdl/pvdl
			// pvdl2 / pvdl2
			mAuthTask = null;
			showProgress(false);

			if (goodLogin) {
		    	Intent i = new Intent(saved_ctxt, MainActivity.class);
		    	i.putExtra("username", mEmail);
		    	i.putExtra("password", mPassword);
				startActivity(i);
				finish();
			} else {
				((Activity) saved_ctxt).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(saved_ctxt, "Login/register failed, try again", Toast.LENGTH_LONG).show();
					}
				});	            
//
//				mPasswordView
//						.setError(getString(R.string.error_incorrect_password));
//				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
