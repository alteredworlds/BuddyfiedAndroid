package com.alteredworlds.buddyfied;

/**
 * Created by twcgilbert on 04/09/2014.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MessageUserFragment extends Fragment {

    public MessageUserFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_message_user, container, false);

        getActivity().setTitle(getString(R.string.message_user_action_title));
        TextView toUser = (TextView) rootView.findViewById(R.id.message_recipient);
        toUser.setText("To: " + getActivity().getIntent().getStringExtra(BuddyFragment.BUDDY_NAME_EXTRA));

        return rootView;
    }
}
