package com.alteredworlds.buddyfied;

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
