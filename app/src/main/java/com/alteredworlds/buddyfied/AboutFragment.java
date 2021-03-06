/*
 * Copyright (c) 2014 Tom Gilbert <tom@alteredworlds.com> - All rights reserved.
 *
 * This file is part of Buddyfied Android.
 *
 * For applicable license please see LICENSE included with this distribution.
 */

package com.alteredworlds.buddyfied;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alteredworlds.buddyfied.service.BuddyBackgroundService;
import com.alteredworlds.buddyfied.service.StaticDataService;

public class AboutFragment extends Fragment {

    public AboutFragment() {
    }


    public static void signOut(Activity context) {
        signOut(context, false);
    }

    public static void signOut(Activity context, Boolean joinRequested) {
        // continue with sign out
        Settings.clearPersonalSettings(context);
        //
        Intent clearDataIntent = new Intent(context, BuddyBackgroundService.class);
        clearDataIntent.putExtra(Constants.METHOD_EXTRA, BuddyBackgroundService.ClearDataOnLogout);
        context.startService(clearDataIntent);
        //
        Intent intent = new Intent(context, LoginActivity.class);
        if (joinRequested) {
            Settings.setJoinRequired(context, true);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        context.finish();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        TextView buddyfiedHelpLink = (TextView) rootView.findViewById(R.id.about_help_link_textview);
        buddyfiedHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
        //
        TextView usernameTextView = (TextView) rootView.findViewById(R.id.about_username_text_view);
        usernameTextView.setText(Settings.getUsername(getActivity()));

        TextView signOutTextView = (TextView) rootView.findViewById(R.id.sign_out_action);
        signOutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String signOutAlertTitle = getString(R.string.sign_out_alert_title) +
                        " '" + Settings.getUsername(getActivity()) + "'";
                new AlertDialog.Builder(getActivity())
                        .setTitle(signOutAlertTitle)
                        .setMessage(getString(R.string.sign_out_alert_message))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                signOut(getActivity(), false);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ;
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        TextView joinTextView = (TextView) rootView.findViewById(R.id.join_action);
        if (Settings.isGuestUser(getActivity())) {
            joinTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signOut(getActivity(), true);
                }
            });
        } else {
            joinTextView.setVisibility(View.GONE);
        }

        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        TextView versionTextView = (TextView) rootView.findViewById(R.id.about_version);
        versionTextView.setText("Version " + versionName + "  build " + versionCode);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.about_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_lists:
                refreshStaticData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshStaticData() {
        Intent staticDataIntent = new Intent(getActivity(), StaticDataService.class);
        staticDataIntent.putExtra(Constants.METHOD_EXTRA, StaticDataService.UPDATE_ALL);
        getActivity().startService(staticDataIntent);
    }
}
