package com.alteredworlds.buddyfied;

/**
 * Created by twcgilbert on 04/09/2014.
 */

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.alteredworlds.buddyfied.service.BuddyBackgroundService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MessageUserFragment extends Fragment {

    private static final String LOG_TAG = MessageUserFragment.class.getSimpleName();

    private EditText mMessageSubject;
    private EditText mMessageBody;
    private BroadcastReceiver mMessageReceiver;
    private Boolean mEnableSendMenuItem = true;

    public MessageUserFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_message_user, container, false);

        getActivity().setTitle(getString(R.string.message_user_action_title));

        TextView recipient = (TextView) rootView.findViewById(R.id.message_recipient);
        recipient.setText("To: " + getActivity().getIntent().getStringExtra(BuddyFragment.BUDDY_NAME_EXTRA));

        mMessageSubject = (EditText) rootView.findViewById(R.id.message_title);
        mMessageBody = (EditText) rootView.findViewById(R.id.message_body);

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle results = intent.getBundleExtra(Constants.RESULT_BUNDLE);
                if (null != results) {
                    // we want a code 0 indicating success.
                    int code = results.getInt(Constants.RESULT_CODE, 0);
                    if (0 != code) {
                        // code other than 0 should trigger an alert
                        String description = results.getString(Constants.RESULT_DESCRIPTION, "");
                        if (TextUtils.isEmpty(description)) {
                            description = getString(R.string.message_send_failed_message);
                        }
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.message_send_failed_title))
                                .setMessage(description)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue with delete
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        enableSendMenuItem(true);
                    } else {
                        // SUCCESS - we need to dismiss this view & return
                        getActivity().onBackPressed();
                    }
                }
            }
        };
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register an observer to receive specific named Intents ('events')
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(BuddyBackgroundService.BUDDY_BACKGROUND_SERVICE_RESULT_EVENT));
    }

    @Override
    public void onPause() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    private void enableSendMenuItem(Boolean enable) {
        mEnableSendMenuItem = enable;
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem sendMenuItem = menu.findItem(R.id.action_send_message);
        if (null != sendMenuItem) {
            sendMenuItem.setEnabled(mEnableSendMenuItem);
            if (!mEnableSendMenuItem) {
                Drawable resIcon = getResources().getDrawable(R.drawable.ic_action_send_now);
                resIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                sendMenuItem.setIcon(resIcon);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_send_message) {
            // send the message
            sendMessage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendMessage() {
        // get the subject and body
        String messageSubject = mMessageSubject.getText().toString();
        String messageBody = mMessageBody.getText().toString();
        if (TextUtils.isEmpty(messageSubject) || TextUtils.isEmpty(messageBody)) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.message_invalid_title))
                    .setMessage(getString(R.string.message_invalid_body))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            enableSendMenuItem(false);
            String recipient = getActivity().getIntent().getStringExtra(BuddyFragment.BUDDY_ID_EXTRA);
            Intent intent = new Intent(getActivity(), BuddyBackgroundService.class);
            intent.putExtra(Constants.METHOD_EXTRA, BuddyBackgroundService.SendMessage);
            intent.putExtra(Constants.ID_EXTRA, recipient);
            intent.putExtra(BuddyBackgroundService.SUBJECT_EXTRA, messageSubject);
            intent.putExtra(BuddyBackgroundService.BODY_EXTRA, messageBody);
            getActivity().startService(intent);
        }
    }
}
