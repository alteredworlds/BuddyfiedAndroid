/*
 * Copyright (c) 2014 Tom Gilbert <tom@alteredworlds.com> - All rights reserved.
 *
 * This file is part of Buddyfied Android.
 *
 * For applicable license please see LICENSE included with this distribution.
 */

package com.alteredworlds.buddyfied;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.alteredworlds.buddyfied.data.BuddyfiedDbHelper;
import com.alteredworlds.buddyfied.service.BuddyBackgroundService;
import com.alteredworlds.buddyfied.service.StaticDataService;


public class JoinActivity extends ActionBarActivity {

    private EditText mUsernameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordConfirmEditText;
    private View mFocusDummy;

    private static final String USERNAME_EXTRA = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        mFocusDummy = findViewById(R.id.focus_dummy);
        mUsernameEditText = (EditText) findViewById(R.id.join_username);
        mEmailEditText = (EditText) findViewById(R.id.join_email);
        mPasswordEditText = (EditText) findViewById(R.id.join_password);
        mPasswordConfirmEditText = (EditText) findViewById(R.id.join_confirm_password);

        if (null != savedInstanceState) {
            mUsernameEditText.setText(savedInstanceState.getString(USERNAME_EXTRA));
            mEmailEditText.setText(savedInstanceState.getString(Constants.EMAIL_EXTRA));
            mPasswordEditText.setText(savedInstanceState.getString(Constants.PASSWORD_EXTRA));
            mPasswordConfirmEditText.setText(savedInstanceState.getString(Constants.PASSWORD_EXTRA));
        }
        //
        // load static data if required (i.e. if we don't already have it)
        Intent staticDataIntent = new Intent(this, StaticDataService.class);
        staticDataIntent.putExtra(Constants.METHOD_EXTRA, StaticDataService.GET_ALL_IF_NEEDED);
        startService(staticDataIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String email = mEmailEditText.getText().toString();

        savedInstanceState.putString(USERNAME_EXTRA, username);
        savedInstanceState.putString(Constants.EMAIL_EXTRA, email);
        savedInstanceState.putString(Constants.PASSWORD_EXTRA, password);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.join, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        // see: http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
        // comment by Mazvél at bottom.
        //
        // This code will override the "up" button to behave the same way as the back button so
        // in the case of Listview -> Details -> Back to Listview (and no other options) this is the
        // simplest code to maintain the scrollposition and the content in the listview.
        //
        // Caution: If you can go to another activity from the details activity the up button will
        // return you back to that activity so you will have to manipulate the backbutton history
        // in order for this to work.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_next:
                onNext();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // clear all data in database
        Intent clearDataIntent = new Intent(this, BuddyBackgroundService.class);
        clearDataIntent.putExtra(Constants.METHOD_EXTRA, BuddyBackgroundService.ClearDataOnLogout);
        startService(clearDataIntent);
        super.onBackPressed();
    }

    private void onNext() {
        mFocusDummy.requestFocus();
        // Reset errors.
        mUsernameEditText.setError(null);
        mPasswordEditText.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String email = mEmailEditText.getText().toString();
        String passwordConfirm = mPasswordConfirmEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            mUsernameEditText.setError(getString(R.string.error_field_required));
            focusView = mUsernameEditText;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameEditText.setError(getString(R.string.username_too_short));
            focusView = mUsernameEditText;
            cancel = true;
        } else if (TextUtils.isEmpty(email)) {
            mEmailEditText.setError(getString(R.string.error_field_required));
            focusView = mEmailEditText;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailEditText.setError(getString(R.string.error_invalid_email));
            focusView = mEmailEditText;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordEditText.setError(getString(R.string.error_field_required));
            focusView = mPasswordEditText;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordEditText.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordEditText;
            cancel = true;
        } else if (0 != password.compareTo(passwordConfirm)) {
            mPasswordConfirmEditText.setError(getString(R.string.error_passwords_must_match));
            focusView = mPasswordConfirmEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            // time to move to the next phase of the Join sequence
            Intent updateIntent = new Intent(this, BuddyBackgroundService.class);
            updateIntent.putExtra(Constants.METHOD_EXTRA, BuddyBackgroundService.UpdateJoinProfile);
            updateIntent.putExtra(BuddyBackgroundService.NAME_EXTRA, username);
            startService(updateIntent);
            //
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(Constants.ID_EXTRA, BuddyfiedDbHelper.JOIN_PROFILE_ID);
            intent.putExtra(Constants.USERNAME_EXTRA, username);
            intent.putExtra(Constants.PASSWORD_EXTRA, password);
            intent.putExtra(Constants.EMAIL_EXTRA, email);
            intent.putExtra(ProfileActivity.JOIN_MODE_KEY, true);
            startActivity(intent);
        }
    }

    private boolean isUsernameValid(String value) {
        return value.length() > 3;
    }

    private boolean isPasswordValid(String value) {
        return value.length() >= Constants.PASSWORD_MIN_LEN;
    }

    private boolean isEmailValid(String value) {
        return value.contains("@") && value.contains(".");
    }
}
