package com.alteredworlds.buddyfied;

/**
 * Created by twcgilbert on 01/09/2014.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A placeholder fragment containing a simple view.
 */
public class BuddyFragment extends Fragment {

    public static final String BUDDY_ID_EXTRA = "buddy_id";
    public static final String BUDDY_NAME_EXTRA = "buddy_name";
    public static final String BUDDY_IMAGE_URI_EXTRA = "buddy_image_uri";

    public BuddyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buddy, container, false);
        return rootView;
    }
}
