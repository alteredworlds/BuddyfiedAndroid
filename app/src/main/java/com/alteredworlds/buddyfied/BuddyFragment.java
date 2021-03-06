/*
 * Copyright (c) 2014 Tom Gilbert <tom@alteredworlds.com> - All rights reserved.
 *
 * This file is part of Buddyfied Android.
 *
 * For applicable license please see LICENSE included with this distribution.
 */

package com.alteredworlds.buddyfied;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.view_model.BuddyAdapter;
import com.alteredworlds.buddyfied.view_model.BuddyHeaderListItem;
import com.alteredworlds.buddyfied.view_model.CommentsListItem;
import com.alteredworlds.buddyfied.view_model.LoaderID;
import com.alteredworlds.buddyfied.view_model.LoaderListItem;
import com.alteredworlds.buddyfied.view_model.SearchListItem;


public class BuddyFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String BUDDY_ID_EXTRA = "buddy_id";
    public static final String BUDDY_NAME_EXTRA = "buddy_name";
    public static final String BUDDY_IMAGE_URI_EXTRA = "buddy_image_uri";
    public static final String BUDDY_VOICE_EXTRA = "buddy_voice";
    public static final String BUDDY_AGE_EXTRA = "buddy_age";
    public static final String BUDDY_COMMENTS_EXTRA = "buddy_comments";
    //
    // data that must be transformed via a loader
    public static final String BUDDY_PLATFORM_EXTRA = "buddy_platform";
    public static final String BUDDY_PLAYING_EXTRA = "buddy_playing";
    public static final String BUDDY_GAMEPLAY_EXTRA = "buddy_gameplay";
    public static final String BUDDY_COUNTRY_EXTRA = "buddy_country";
    public static final String BUDDY_LANGUAGE_EXTRA = "buddy_language";
    public static final String BUDDY_SKILL_EXTRA = "buddy_skill";
    public static final String BUDDY_TIME_EXTRA = "buddy_time";


    private final int ROW_INDEX_HEADER;
    private final int ROW_INDEX_PLATFORM;
    private final int ROW_INDEX_PLAYING;
    private final int ROW_INDEX_GAMEPLAY;
    private final int ROW_INDEX_COUNTRY;
    private final int ROW_INDEX_LANGUAGE;
    private final int ROW_INDEX_SKILL;
    private final int ROW_INDEX_TIME;
    private final int ROW_INDEX_AGE;
    private final int ROW_INDEX_VOICE;
    private final int ROW_INDEX_COMMENTS;

    private BuddyAdapter mAdapter;
    private long mBuddyId;

    private LoaderListItem[] mData;

    public BuddyFragment() {
        mData = new LoaderListItem[]{
                new BuddyHeaderListItem("", ""),
                new SearchListItem("Platform", "", "", LoaderID.BUDDY_PLATFORM),
                new SearchListItem("Playing", "", "", LoaderID.BUDDY_PLAYING),
                new SearchListItem("Gameplay", "", "", LoaderID.BUDDY_GAMEPLAY),
                new SearchListItem("Country", "", "", LoaderID.BUDDY_COUNTRY),
                new SearchListItem("Language", "", "", LoaderID.BUDDY_LANGUAGE),
                new SearchListItem("Skill", "", "", LoaderID.BUDDY_SKILL),
                new SearchListItem("Time", "", "", LoaderID.BUDDY_TIME),
                new SearchListItem("Age", "", "", LoaderID.NONE),
                new SearchListItem("Voice", "", "", LoaderID.BUDDY_VOICE),
                new CommentsListItem("Comments", "", "")
        };
        ROW_INDEX_HEADER = 0;
        ROW_INDEX_PLATFORM = 1;
        ROW_INDEX_PLAYING = 2;
        ROW_INDEX_GAMEPLAY = 3;
        ROW_INDEX_COUNTRY = 4;
        ROW_INDEX_LANGUAGE = 5;
        ROW_INDEX_SKILL = 6;
        ROW_INDEX_TIME = 7;
        ROW_INDEX_AGE = 8;
        ROW_INDEX_VOICE = 9;
        ROW_INDEX_COMMENTS = 10;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buddy, container, false);

        Intent intent = getActivity().getIntent();
        mBuddyId = intent.getLongExtra(BUDDY_ID_EXTRA, -1);
        mData[ROW_INDEX_HEADER].name = intent.getStringExtra(BUDDY_NAME_EXTRA);
        mData[ROW_INDEX_HEADER].value = intent.getStringExtra(BUDDY_IMAGE_URI_EXTRA);

        // data we can already just use is assigned to 'value'
        mData[ROW_INDEX_VOICE].value = intent.getStringExtra(BUDDY_VOICE_EXTRA);
        mData[ROW_INDEX_AGE].value = intent.getStringExtra(BUDDY_AGE_EXTRA);
        mData[ROW_INDEX_COMMENTS].value = intent.getStringExtra(BUDDY_COMMENTS_EXTRA);
        //
        // data that must be transformed via a loader assigned to 'extra'
        mData[ROW_INDEX_PLATFORM].extra = intent.getStringExtra(BUDDY_PLATFORM_EXTRA);
        mData[ROW_INDEX_PLAYING].extra = intent.getStringExtra(BUDDY_PLAYING_EXTRA);
        mData[ROW_INDEX_GAMEPLAY].extra = intent.getStringExtra(BUDDY_GAMEPLAY_EXTRA);
        mData[ROW_INDEX_COUNTRY].extra = intent.getStringExtra(BUDDY_COUNTRY_EXTRA);
        mData[ROW_INDEX_LANGUAGE].extra = intent.getStringExtra(BUDDY_LANGUAGE_EXTRA);
        mData[ROW_INDEX_SKILL].extra = intent.getStringExtra(BUDDY_SKILL_EXTRA);
        mData[ROW_INDEX_TIME].extra = intent.getStringExtra(BUDDY_TIME_EXTRA);

        mAdapter = new BuddyAdapter(getActivity(), mData);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_buddy);
        listView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        for (int i = 0; i < mData.length; i++) {
            if (LoaderID.NONE != mData[i].loaderId) {
                getLoaderManager().initLoader(mData[i].loaderId, null, this);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> retVal = null;
        // find the row information for this loader id
        LoaderListItem row = null;
        for (int i = 0; i < mData.length; i++) {
            if (mData[i].loaderId == id) {
                row = mData[i];
                break;
            }
        }
        if (null != row) {
            //we have the comma separated list of AttributeEntry._ID values in row.extra
            final String select = AttributeEntry._ID + " IN (" + row.extra + ")";
            retVal = new CursorLoader(
                    getActivity(),
                    AttributeEntry.CONTENT_URI,
                    new String[]{AttributeEntry.COLUMN_NAME},
                    select,
                    null,
                    AttributeEntry.COLUMN_NAME + " ASC");
        }
        return retVal;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();

        // find the correct ProfileRow to update based on the loaderId
        LoaderListItem row = null;
        for (int i = 0; i < mData.length; i++) {
            if (mData[i].loaderId == loaderId) {
                row = mData[i];
                break;
            }
        }
        if (null != row) {
            StringBuilder sb = new StringBuilder();
            if (data.moveToFirst()) {
                sb.append(data.getString(0));
                while (data.moveToNext()) {
                    sb.append("\n" + data.getString(0));
                }
            }
            row.value = sb.toString();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
