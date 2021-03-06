/*
 * Copyright (c) 2014 Tom Gilbert <tom@alteredworlds.com> - All rights reserved.
 *
 * This file is part of Buddyfied Android.
 *
 * For applicable license please see LICENSE included with this distribution.
 */

package com.alteredworlds.buddyfied;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;


public class BuddyActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddy);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new BuddyFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.buddy, menu);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void messageUserButtonClick(View view) {
        if (Settings.isGuestUser(this)) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.guest_user_message_alert_title))
                    .setMessage(getString(R.string.guest_user_message_alert_message))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            Intent intent = new Intent(this, MessageUserActivity.class);
            intent.putExtra(BuddyFragment.BUDDY_NAME_EXTRA, getIntent().getStringExtra(BuddyFragment.BUDDY_NAME_EXTRA));
            intent.putExtra(BuddyFragment.BUDDY_ID_EXTRA, String.valueOf(getIntent().getLongExtra(BuddyFragment.BUDDY_ID_EXTRA, -1)));
            startActivity(intent);
        }
    }

    public void reportUserButtonClick(View view) {
        String body = getString(R.string.buddy_report_user_email_body1) +
                " " +
                getIntent().getStringExtra(BuddyFragment.BUDDY_NAME_EXTRA) +
                getString(R.string.buddy_report_user_email_body2) +
                " " +
                Settings.getUsername(this);

        Intent sendIntent = createEmailIntent(
                getString(R.string.buddy_report_user_email),
                getString(R.string.buddy_report_user_email_subject),
                body);
        if (null != sendIntent) {
            startActivity(sendIntent);
        }
    }

    public Intent createEmailIntent(final String toEmail,
                                    final String subject,
                                    final String message) {
        Intent sendTo = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode(toEmail) +
                "?subject=" + Uri.encode(subject);
        Uri uri = Uri.parse(uriText);
        sendTo.setData(uri);
        sendTo.putExtra(Intent.EXTRA_TEXT, message);

        List<ResolveInfo> resolveInfos =
                getPackageManager().queryIntentActivities(sendTo, 0);

        // Emulators may not like this check...
        if (!resolveInfos.isEmpty()) {
            return sendTo;
        }

        // Nothing resolves send to, so fallback to send...
        Intent send = new Intent(Intent.ACTION_SEND);

        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_EMAIL,
                new String[]{toEmail});
        send.putExtra(Intent.EXTRA_SUBJECT, subject);
        send.putExtra(Intent.EXTRA_TEXT, message);

        return Intent.createChooser(send, getString(R.string.email_chooser_title));
    }
}
