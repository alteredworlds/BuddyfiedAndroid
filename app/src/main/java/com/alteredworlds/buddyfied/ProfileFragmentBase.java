/*
 * Copyright (c) 2014 Tom Gilbert <tom@alteredworlds.com> - All rights reserved.
 *
 * This file is part of Buddyfied Android.
 *
 * For applicable license please see LICENSE included with this distribution.
 */

package com.alteredworlds.buddyfied;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alteredworlds.buddyfied.data.BuddyfiedContract;
import com.alteredworlds.buddyfied.view_model.BuddyAdapter;
import com.alteredworlds.buddyfied.view_model.LoaderID;
import com.alteredworlds.buddyfied.view_model.LoaderListItem;

public class ProfileFragmentBase extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = ProfileFragmentBase.class.getSimpleName();

    protected BuddyAdapter mAdapter;
    protected int COMMENTS_ROW;
    protected int AGE_ROW;
    protected int HEADER_ROW;
    protected long mProfileId;
    protected LoaderListItem[] mData;

    private static final String[] ProfileColumns = {
            BuddyfiedContract.ProfileEntry.COLUMN_NAME,
            BuddyfiedContract.ProfileEntry.COLUMN_IMAGE_URI,
            BuddyfiedContract.ProfileEntry.COLUMN_AGE,
            BuddyfiedContract.ProfileEntry.COLUMN_COMMENTS
    };
    private static final int COL_NAME = 0;
    private static final int COL_IMAGE_URI = 1;
    private static final int COL_AGE = 2;
    private static final int COL_COMMENTS = 3;

    public ProfileFragmentBase() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mProfileId = getActivity().getIntent().getLongExtra(Constants.ID_EXTRA, -1);
        if (-1 == mProfileId) {
            mProfileId = Settings.getUserId(getActivity());
        }
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mAdapter = new BuddyAdapter(getActivity(), mData);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_profile);
        listView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // start loaders
        getLoaderManager().initLoader(LoaderID.PROFILE_MAIN, null, this);
        for (int i = 0; i < mData.length; i++) {
            if (LoaderID.NONE != mData[i].loaderId) {
                getLoaderManager().initLoader(mData[i].loaderId, null, this);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> retVal = null;
        if (LoaderID.PROFILE_MAIN == id) {
            Uri query = BuddyfiedContract.ProfileEntry.buildProfileUri(mProfileId);
            retVal = new CursorLoader(
                    getActivity(),
                    query,
                    ProfileColumns,
                    null,
                    null,
                    null);

        } else {
            // find the row information for this loader id
            LoaderListItem row = null;
            for (int i = 0; i < mData.length; i++) {
                if (mData[i].loaderId == id) {
                    row = mData[i];
                    break;
                }
            }
            if ((null != row) && !TextUtils.isEmpty(row.extra)) {
                // we have the AttributeEntry.COLUMN_TYPE information in row.extra
                Uri query = BuddyfiedContract.AttributeEntry.buildAttributeTypeForProfile(row.extra, mProfileId);
                retVal = new CursorLoader(
                        getActivity(),
                        query,
                        new String[]{BuddyfiedContract.AttributeEntry.COLUMN_NAME},
                        null,
                        null,
                        BuddyfiedContract.AttributeEntry.COLUMN_NAME + " ASC");
            }
        }
        return retVal;
    }

    protected String getProfileName() {
        return mData[HEADER_ROW].name;
    }

    protected void setProfileName(String name) {
        setRowName(HEADER_ROW, name);
    }

    protected void setProfileImageUri(String imageUri) {
        setRowValue(HEADER_ROW, imageUri);
    }

    protected void setAge(String age) {
        setRowValue(AGE_ROW, age);
    }

    protected void setComments(String comments) {
        setRowValue(COMMENTS_ROW, comments);
    }

    protected void setRowName(int row, String value) {
        if ((-1 != row) && (mData.length > row)) {
            mData[row].name = value;
        }
    }

    protected void setRowValue(int row, String value) {
        if ((-1 != row) && (mData.length > row)) {
            mData[row].value = value;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        if (LoaderID.PROFILE_MAIN == loaderId) {
            // we can unpack AGE, COMMENTS, IMAGE
            if (data.moveToFirst()) {
                setComments(data.getString(COL_COMMENTS));
                setAge(data.getString(COL_AGE));
                setProfileName(data.getString(COL_NAME));
                setProfileImageUri(data.getString(COL_IMAGE_URI));
            }
        } else {
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
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

