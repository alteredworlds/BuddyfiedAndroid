package com.alteredworlds.buddyfied;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.alteredworlds.buddyfied.service.BuddyUserService;

/**
 * Created by twcgilbert on 26/09/2014.
 */
public class ProfileFragmentEdit extends ProfileFragmentMaleable {
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
                        // profile amendment has succeeded - time to close out and return
                        //commit edited profile doesn't seem to work, or at least update UI
                        // on readonly profile view. so forget it for now, not necessary.
//                        Intent commitIntent = new Intent(getActivity(), BuddyQueryService.class);
//                        commitIntent.putExtra(Constants.METHOD_EXTRA, BuddyQueryService.CommitEditProfile);
//                        getActivity().startService(commitIntent);
                        //
                        getActivity().finish();
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
                        // re-enable the DONE button to allow option of having another go
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
            nextMenuItem.setTitle(R.string.action_commit);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next:
                onCommit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onCommit() {
        if (validateProfile()) {
            setNextButtonEnabled(false);
            showProgress(true);
            Intent intent = new Intent(getActivity(), BuddyUserService.class);
            intent.putExtra(Constants.METHOD_EXTRA, BuddyUserService.UPDATE);
            intent.putExtra(Constants.ID_EXTRA, mProfileId);
            intent.putExtra(Constants.PASSWORD_EXTRA, Settings.getPassword(getActivity()));
            getActivity().startService(intent);
        }
    }
}
