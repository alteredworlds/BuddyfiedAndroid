package com.alteredworlds.buddyfied;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;
import com.alteredworlds.buddyfied.service.BuddyQueryService;


public class JoinActivity extends ActionBarActivity {

    private EditText mUsernameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordConfirmEditText;
    private View mFocusDummy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        mFocusDummy = findViewById(R.id.focus_dummy);

        mUsernameEditText = (EditText) findViewById(R.id.join_username);
        mUsernameEditText.setText(Settings.getUsername(this));

        mEmailEditText = (EditText) findViewById(R.id.join_email);
        mEmailEditText.setText(Settings.getEmail(this));

        mPasswordEditText = (EditText) findViewById(R.id.join_password);
        mPasswordEditText.setText(Settings.getPassword(this));

        mPasswordConfirmEditText = (EditText) findViewById(R.id.join_confirm_password);
        mPasswordConfirmEditText.setText(Settings.getPassword(this));
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
        // clear username, password, email
        Settings.clearPersonalSettings(this);
        // clear all data in database
        Intent clearDataIntent = new Intent(this, BuddyQueryService.class);
        clearDataIntent.putExtra(Constants.METHOD_EXTRA, BuddyQueryService.ClearDataOnLogout);
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
            //
            // first, persist the values derived from this form
            Settings.setUsername(this, username);
            Settings.setPassword(this, password);
            Settings.setEmail(this, email);
            //
            long userId = createUserProfileIfNeeded(username);
            Settings.setUserId(this, userId);
            //
            // now transition to the
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }
    }

    private long createUserProfileIfNeeded(String name) {
        long retVal = -1;
        Cursor cursor = getContentResolver().query(ProfileEntry.CONTENT_URI,
                new String[]{ProfileEntry._ID},
                ProfileEntry.COLUMN_NAME + " = '" + name + "'",
                null, null);
        if ((null != cursor) && cursor.moveToFirst()) {
            // we have a user profile already, just get the ID
            retVal = cursor.getLong(0);
        } else {
            ContentValues row = new ContentValues();
            row.put(ProfileEntry.COLUMN_NAME, name);
            Uri rowUri = getContentResolver().insert(ProfileEntry.CONTENT_URI, row);
            retVal = ContentUris.parseId(rowUri);
        }
        return retVal;
    }

    private boolean isUsernameValid(String value) {
        return value.length() > 3;
    }

    private boolean isPasswordValid(String value) {
        return value.length() > 4;
    }

    private boolean isEmailValid(String value) {
        return value.contains("@") && value.contains(".");
    }
}
