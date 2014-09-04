package com.alteredworlds.buddyfied;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MessageUserActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_user);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MessageUserFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.message_user, menu);
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
                return (true);
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
