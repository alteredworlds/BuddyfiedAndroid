package com.alteredworlds.buddyfied;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alteredworlds.buddyfied.service.BuddyQueryService;

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

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), BuddyQueryService.class);
        intent.putExtra(BuddyQueryService.METHOD_EXTRA, BuddyQueryService.GetMatches);
        intent.putExtra(BuddyQueryService.PROFILE_ID_EXTRA, ((MainActivity) getActivity()).getSearchProfileId());
        getActivity().startService(intent);
    }
}