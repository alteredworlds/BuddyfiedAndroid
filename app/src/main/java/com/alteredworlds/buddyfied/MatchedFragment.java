package com.alteredworlds.buddyfied;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by twcgilbert on 30/07/2014.
 */
public class MatchedFragment extends Fragment {

    public MatchedFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_matched, container, false);
        return rootView;
    }
}