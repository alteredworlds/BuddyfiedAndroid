package com.alteredworlds.buddyfied;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by twcgilbert on 30/07/2014.
 */
public class AboutFragment extends Fragment {

    public AboutFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        TextView buddyfiedHelpLink = (TextView)rootView.findViewById(R.id.about_help_link_textview);
        buddyfiedHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
        //
        TextView usernameTextView = (TextView)rootView.findViewById(R.id.about_username_text_view);
        usernameTextView.setText(Settings.getUsername(getActivity()));

        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        TextView versionTextView = (TextView)rootView.findViewById(R.id.about_version);
        versionTextView.setText("Version " + versionName + "  build " + versionCode);
        return rootView;
    }
}
