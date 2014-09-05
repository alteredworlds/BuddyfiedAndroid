package com.alteredworlds.buddyfied;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.alteredworlds.buddyfied.service.BuddyQueryService;


/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends Activity {

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private Button mSignInButton;
    private Button mGuestSignInButton;
    private Button mForgotPasswordButton;

    private BroadcastReceiver mMessageReceiver;
    private View mFocusDummy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.isNullOrEmpty(Settings.getUsername(this)) &&
                !Utils.isNullOrEmpty(Settings.getPassword(this))) {
            //we're logged in already, just move on to app
            Intent main = new Intent(this, MainActivity.class);
            this.startActivity(main);
            finish();
        }
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.email);

        mFocusDummy = findViewById(R.id.focus_dummy);

        mPasswordView = (EditText) findViewById(R.id.password);

        mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mGuestSignInButton = (Button) findViewById(R.id.guest_sign_in_button);
        mGuestSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptGuestLogin();
            }
        });

        mForgotPasswordButton = (Button) findViewById(R.id.forgot_password_button);
        mForgotPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPassword();
            }
        });

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle results = intent.getBundleExtra(BuddyQueryService.RESULT_BUNDLE);
                if (null != results) {
                    // we want a code 0 indicating success.
                    int code = results.getInt(BuddyQueryService.RESULT_CODE, 0);
                    if (0 != code) {
                        // code other than 0 should trigger an alert
                        Settings.setUsername(context, "");
                        Settings.setPassword(context, "");
                        String description = results.getString(BuddyQueryService.RESULT_DESCRIPTION, "");
                        if (Utils.isNullOrEmpty(description)) {
                            description = getString(R.string.sign_in_failed_message_default);
                        }
                        showErrorAlert(description);
                        enableButtons(true);
                    } else {
                        // LOGIN SUCCESSFUL...
                        Intent main = new Intent(context, MainActivity.class);
                        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(main);
                        finish();
                    }
                }
            }
        };

    }

    private void showErrorAlert(String description) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.sign_in_failed_title))
                .setMessage(description)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register an observer to receive specific named Intents ('events')
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(BuddyQueryService.BUDDY_QUERY_SERVICE_RESULT_EVENT));
    }

    @Override
    public void onPause() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    private void enableButtons(Boolean enabled) {
        mGuestSignInButton.setEnabled(enabled);
        mSignInButton.setEnabled(enabled);
        mForgotPasswordButton.setEnabled(enabled);
    }

    public void forgotPassword() {
        ;
    }

    public void attemptGuestLogin() {
        enableButtons(false);
        mFocusDummy.requestFocus();
        Settings.setUsername(this, Settings.getGuestUsername(this));
        Settings.setPassword(this, Settings.getGuestPassword(this));
        Intent intent = new Intent(this, BuddyQueryService.class);
        intent.putExtra(BuddyQueryService.METHOD_EXTRA, BuddyQueryService.VerifyConnection);
        startService(intent);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        enableButtons(false);
        mFocusDummy.requestFocus();
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            // TRY LOGGING IN NOW...
            Settings.setUsername(this, username);
            Settings.setPassword(this, password);
            Intent intent = new Intent(this, BuddyQueryService.class);
            intent.putExtra(BuddyQueryService.METHOD_EXTRA, BuddyQueryService.VerifyConnection);
            startService(intent);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }
}



