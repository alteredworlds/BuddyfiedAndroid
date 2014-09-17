package com.alteredworlds.buddyfied;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alteredworlds.buddyfied.service.BuddyQueryService;

/**
 * Created by twcgilbert on 30/07/2014.
 */
public class AboutFragment extends Fragment {

    public AboutFragment() {
    }

    private void signOut(Boolean joinRequested) {
        // continue with sign out
        Settings.setPassword(getActivity(), "");
        Settings.setUsername(getActivity(), "");
        Settings.setUserId(getActivity(), -1);
        Settings.setPosition(getActivity(), 0);
        //
        Intent clearDataIntent = new Intent(getActivity(), BuddyQueryService.class);
        clearDataIntent.putExtra(Constants.METHOD_EXTRA, BuddyQueryService.ClearDataOnLogout);
        getActivity().startService(clearDataIntent);
        //
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        if (joinRequested) {
            Settings.setJoinRequired(getActivity(), true);
        }
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
                                signOut(false);
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
                    signOut(true);
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
}
