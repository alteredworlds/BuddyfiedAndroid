package com.alteredworlds.buddyfied;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.alteredworlds.buddyfied.service.BuddyUserService;

/**
 * Created by twcgilbert on 26/09/2014.
 */
public class ProfileFragmentJoin extends ProfileFragmentMaleable {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showProgress(false);
                Bundle results = intent.getBundleExtra(Constants.RESULT_BUNDLE);
                if (null != results) {
                    // we want a code 0 indicating success.
                    int code = results.getInt(Constants.RESULT_CODE, Constants.RESULT_OK);
                    String description = results.getString(Constants.RESULT_DESCRIPTION, "");
                    if (Constants.RESULT_OK == code) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.welcome_title) + " " + getProfileName() + "!")
                                .setMessage(getString(R.string.welcome_message))
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        AboutFragment.signOut(getActivity());
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    } else {
                        // error case & popup an alert with error
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.user_registration_failed))
                                .setMessage(description)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        // re-enable the JOIN button to allow option of having another go
                        setNextButtonEnabled(true);
                    }
                }
            }
        };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem nextMenuItem = menu.findItem(R.id.action_next);
        if (null != nextMenuItem) {
            nextMenuItem.setTitle(R.string.join_action);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next:
                onJoin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onJoin() {
        if (validateProfile()) {
            setNextButtonEnabled(false);
            LayoutInflater li = LayoutInflater.from(getActivity());
            View promptsView = li.inflate(R.layout.eula_prompt, null);
            TextView tv = (TextView) promptsView.findViewById(R.id.eula_link_textview);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            new AlertDialog.Builder(
                    getActivity())
                    .setView(promptsView)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(getActivity(), BuddyUserService.class);
                                    intent.putExtra(Constants.METHOD_EXTRA, BuddyUserService.REGISTER);
                                    intent.putExtra(Constants.ID_EXTRA, mProfileId);
                                    intent.putExtra(
                                            Constants.USERNAME_EXTRA,
                                            getActivity().getIntent().getStringExtra(Constants.USERNAME_EXTRA));
                                    intent.putExtra(
                                            Constants.EMAIL_EXTRA,
                                            getActivity().getIntent().getStringExtra(Constants.EMAIL_EXTRA));
                                    intent.putExtra(
                                            Constants.PASSWORD_EXTRA,
                                            getActivity().getIntent().getStringExtra(Constants.PASSWORD_EXTRA));
                                    getActivity().startService(intent);
                                    showProgress(true);
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    setNextButtonEnabled(true);
                                }
                            })
                    .create()
                    .show();
        }
    }
}
