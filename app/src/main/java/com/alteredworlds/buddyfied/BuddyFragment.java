package com.alteredworlds.buddyfied;

/**
 * Created by twcgilbert on 01/09/2014.
 */

import android.database.Cursor;
import android.net.Uri;
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
import com.alteredworlds.buddyfied.data.BuddyfiedContract.BuddyEntry;
import com.alteredworlds.buddyfied.view_model.BuddyAdapter;
import com.alteredworlds.buddyfied.view_model.BuddyHeaderListItem;
import com.alteredworlds.buddyfied.view_model.CommentsListItem;
import com.alteredworlds.buddyfied.view_model.LoaderListItem;
import com.alteredworlds.buddyfied.view_model.SearchListItem;


/**
 * A placeholder fragment containing a simple view.
 */
public class BuddyFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String BUDDY_ID_EXTRA = "buddy_id";
    public static final String BUDDY_NAME_EXTRA = "buddy_name";
    public static final String BUDDY_IMAGE_URI_EXTRA = "buddy_image_uri";

    public static final String[] BuddyColumns = {
            BuddyEntry._ID,
            BuddyEntry.COLUMN_NAME,
            BuddyEntry.COLUMN_COMMENTS,
            BuddyEntry.COLUMN_IMAGE_URI,
            BuddyEntry.COLUMN_AGE,
            // comma separated server ID lists for each type of attribute
            BuddyEntry.COLUMN_COUNTRY,
            BuddyEntry.COLUMN_GAMEPLAY,
            BuddyEntry.COLUMN_LANGUAGE,
            BuddyEntry.COLUMN_PLATFORM,
            BuddyEntry.COLUMN_PLAYING,
            BuddyEntry.COLUMN_SKILL,
            BuddyEntry.COLUMN_TIME,
            BuddyEntry.COLUMN_VOICE
    };

    private static final int COL_NAME = 1;
    private static final int COL_COMMENTS = 2;
    private static final int COL_IMAGE_URI = 3;
    private static final int COL_AGE = 4;
    private static final int COL_COUNTRY = 5;
    private static final int COL_GAMEPLAY = 6;
    private static final int COL_LANGUAGE = 7;
    private static final int COL_PLATFORM = 8;
    private static final int COL_PLAYING = 9;
    private static final int COL_SKILL = 10;
    private static final int COL_TIME = 11;
    private static final int COL_VOICE = 12;

    private static final int LOADER_ID_PLATFORM = 0;
    private static final int LOADER_ID_PLAYING = 1;
    private static final int LOADER_ID_GAMEPLAY = 2;
    private static final int LOADER_ID_COUNTRY = 3;
    private static final int LOADER_ID_LANGUAGE = 4;
    private static final int LOADER_ID_SKILL = 5;
    private static final int LOADER_ID_TIME = 6;
    private static final int LOADER_ID_VOICE = 7;
    private static final int LOADER_ID_BUDDY = 8;

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
                new SearchListItem("Platform", "", "", LOADER_ID_PLATFORM),
                new SearchListItem("Playing", "", "", LOADER_ID_PLAYING),
                new SearchListItem("Gameplay", "", "", LOADER_ID_GAMEPLAY),
                new SearchListItem("Country", "", "", LOADER_ID_COUNTRY),
                new SearchListItem("Language", "", "", LOADER_ID_LANGUAGE),
                new SearchListItem("Skill", "", "", LOADER_ID_SKILL),
                new SearchListItem("Time", "", "", LOADER_ID_TIME),
                new SearchListItem("Age", "", "", LoaderListItem.LOADER_ID_NONE),
                new SearchListItem("Voice", "", "", LOADER_ID_VOICE),
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

        mBuddyId = getActivity().getIntent().getLongExtra(BUDDY_ID_EXTRA, -1);
        mData[ROW_INDEX_HEADER].name = getActivity().getIntent().getStringExtra(BUDDY_NAME_EXTRA);
        mData[ROW_INDEX_HEADER].value = getActivity().getIntent().getStringExtra(BUDDY_IMAGE_URI_EXTRA);

        mAdapter = new BuddyAdapter(getActivity(), mData);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_buddy);
        listView.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID_BUDDY, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_ID_BUDDY, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (LOADER_ID_BUDDY == id) {
            // first we need to get the buddy information from the database
            final Uri query = BuddyEntry.buildBuddyUri(mBuddyId);
            return new CursorLoader(
                    getActivity(),
                    query,
                    BuddyColumns,
                    null,
                    null,
                    null);
        } else {
            int idx = -1;
            switch (id) {
                case LOADER_ID_PLATFORM:
                    idx = ROW_INDEX_PLATFORM;
                    break;
                case LOADER_ID_PLAYING:
                    idx = ROW_INDEX_PLAYING;
                    break;
                case LOADER_ID_GAMEPLAY:
                    idx = ROW_INDEX_GAMEPLAY;
                    break;
                case LOADER_ID_COUNTRY:
                    idx = ROW_INDEX_COUNTRY;
                    break;
                case LOADER_ID_LANGUAGE:
                    idx = ROW_INDEX_LANGUAGE;
                    break;
                case LOADER_ID_SKILL:
                    idx = ROW_INDEX_SKILL;
                    break;
                case LOADER_ID_TIME:
                    idx = ROW_INDEX_TIME;
                    break;
                case LOADER_ID_VOICE:
                    idx = ROW_INDEX_VOICE;
                    break;
            }
            final String idList = mData[idx].extra;
            final String select = AttributeEntry._ID + " IN (" + idList + ")";
            return new CursorLoader(
                    getActivity(),
                    AttributeEntry.CONTENT_URI,
                    new String[]{AttributeEntry.COLUMN_NAME},
                    select,
                    null,
                    AttributeEntry.COLUMN_NAME + " ASC");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        if (LOADER_ID_BUDDY == loaderId) {
            if ((null != data) && data.moveToFirst()) {
                // data we can already just use is assigned to 'value'
                mData[ROW_INDEX_VOICE].value = data.getString(COL_VOICE);
                mData[ROW_INDEX_AGE].value = data.getString(COL_AGE);
                mData[ROW_INDEX_COMMENTS].value = data.getString(COL_COMMENTS);
                //
                // data that must be transformed via a loader assigned to 'extra'
                mData[ROW_INDEX_PLATFORM].extra = data.getString(COL_PLATFORM);
                mData[ROW_INDEX_PLAYING].extra = data.getString(COL_PLAYING);
                mData[ROW_INDEX_GAMEPLAY].extra = data.getString(COL_GAMEPLAY);
                mData[ROW_INDEX_COUNTRY].extra = data.getString(COL_COUNTRY);
                mData[ROW_INDEX_LANGUAGE].extra = data.getString(COL_LANGUAGE);
                mData[ROW_INDEX_SKILL].extra = data.getString(COL_SKILL);
                mData[ROW_INDEX_TIME].extra = data.getString(COL_TIME);
                //
                // kick off the [sub]loaders to transform this data
                for (int i = 0; i < mData.length; i++) {
                    if (LoaderListItem.LOADER_ID_NONE != mData[i].loaderId) {
                        getLoaderManager().initLoader(mData[i].loaderId, null, this);
                    }
                }
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
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
