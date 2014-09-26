package com.alteredworlds.buddyfied;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;


public class ProfileActivity extends ActionBarActivity {

    public static final String JOIN_MODE_KEY = "join_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (savedInstanceState == null) {
            // when launched via ProfileActivity, appearing either as:
            // Part of JOIN sequence
            //   OR
            // EDIT profile of logged in user
            ProfileFragmentBase frag = null;
            if (getIntent().getBooleanExtra(JOIN_MODE_KEY, false)) {
                frag = new ProfileFragmentJoin();
            } else {
                frag = new ProfileFragmentEdit();
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, frag)
                    .commit();
        }
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
