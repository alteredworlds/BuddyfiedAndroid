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
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alteredworlds.buddyfied.service.BuddyBackgroundService;


/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends Activity {
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private Button mSignInButton;
    private Button mGuestSignInButton;
    private Button mJoinButton;
    private TextView mForgotPasswordLink;

    private BroadcastReceiver mMessageReceiver;
    private View mFocusDummy;

    private Boolean alreadyLoggedIn() {
        return !TextUtils.isEmpty(Settings.getUsername(this)) &&
                !TextUtils.isEmpty(Settings.getPassword(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (alreadyLoggedIn()) {
            //we're logged in already, just move on to app
            Intent main = new Intent(this, MainActivity.class);
            this.startActivity(main);
            finish();
        } else {
            // cleanup in case any previous join attempt has left a profile
            // record in place. This works; no doubt there's a better way!
            Intent clearDataIntent = new Intent(this, BuddyBackgroundService.class);
            clearDataIntent.putExtra(Constants.METHOD_EXTRA, BuddyBackgroundService.ClearDataOnLogout);
            startService(clearDataIntent);
        }
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.login_username);

        mFocusDummy = findViewById(R.id.focus_dummy);

        mPasswordView = (EditText) findViewById(R.id.login_password);

        mSignInButton = (Button) findViewById(R.id.login_sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mGuestSignInButton = (Button) findViewById(R.id.login_guest_button);
        mGuestSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptGuestLogin();
            }
        });

        mJoinButton = (Button) findViewById(R.id.login_join_button);
        mJoinButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptJoin();
            }
        });

        mForgotPasswordLink = (TextView) findViewById(R.id.forgot_password_link);
        mForgotPasswordLink.setMovementMethod(LinkMovementMethod.getInstance());

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle results = intent.getBundleExtra(Constants.RESULT_BUNDLE);
                if (null != results) {
                    // we want a code 0 indicating success.
                    int code = results.getInt(Constants.RESULT_CODE, 0);
                    if (0 != code) {
                        // code other than 0 should trigger an alert
                        Settings.setUsername(context, "");
                        Settings.setPassword(context, "");
                        String description = results.getString(Constants.RESULT_DESCRIPTION, "");
                        if (TextUtils.isEmpty(description)) {
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
        enableButtons(true);
        // Register an observer to receive specific named Intents ('events')
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(BuddyBackgroundService.BUDDY_BACKGROUND_SERVICE_RESULT_EVENT));
        if (Settings.getJoinRequired(this)) {
            Settings.setJoinRequired(this, false);
            attemptJoin();
        }
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
        mForgotPasswordLink.setEnabled(enabled);
        mJoinButton.setEnabled(enabled);
    }

    private void attemptJoin() {
        cleanupActiveStatePriorToTransition();
        Intent joinIntent = new Intent(this, JoinActivity.class);
        startActivity(joinIntent);
    }

    public void attemptGuestLogin() {
        cleanupActiveStatePriorToTransition();
        Settings.setUsername(this, Settings.getGuestUsername(this));
        Settings.setPassword(this, Settings.getGuestPassword(this));
        Intent intent = new Intent(this, BuddyBackgroundService.class);
        intent.putExtra(Constants.METHOD_EXTRA, BuddyBackgroundService.VerifyConnection);
        startService(intent);
    }

    private void cleanupActiveStatePriorToTransition() {
        enableButtons(false);
        mFocusDummy.requestFocus();
        resetErrors();
    }

    private void resetErrors() {
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        cleanupActiveStatePriorToTransition();

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
            enableButtons(true);
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            // TRY LOGGING IN NOW...
            Settings.setUsername(this, username);
            Settings.setPassword(this, password);
            Intent intent = new Intent(this, BuddyBackgroundService.class);
            intent.putExtra(Constants.METHOD_EXTRA, BuddyBackgroundService.VerifyConnection);
            startService(intent);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= Constants.PASSWORD_MIN_LEN;
    }
}



