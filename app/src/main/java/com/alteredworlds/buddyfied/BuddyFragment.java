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
import android.widget.TextView;

import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.BuddyEntry;
import com.alteredworlds.buddyfied.view_model.ProfileRow;
import com.alteredworlds.buddyfied.view_model.SearchAdapter;


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
    private static final int LOADER_ID_NONE = -1;

    private final int ROW_INDEX_PLATFORM;
    private final int ROW_INDEX_PLAYING;
    private final int ROW_INDEX_GAMEPLAY;
    private final int ROW_INDEX_COUNTRY;
    private final int ROW_INDEX_LANGUAGE;
    private final int ROW_INDEX_SKILL;
    private final int ROW_INDEX_TIME;
    private final int ROW_INDEX_AGE;
    private final int ROW_INDEX_VOICE;

    private SearchAdapter mAdapter;

    private ProfileRow[] mData;
    private TextView mCommentTextView;

    public BuddyFragment() {
        mData = new ProfileRow[]{
                new ProfileRow("Platform", "", "", LOADER_ID_PLATFORM),
                new ProfileRow("Playing", "", "", LOADER_ID_PLAYING),
                new ProfileRow("Gameplay", "", "", LOADER_ID_GAMEPLAY),
                new ProfileRow("Country", "", "", LOADER_ID_COUNTRY),
                new ProfileRow("Language", "", "", LOADER_ID_LANGUAGE),
                new ProfileRow("Skill", "", "", LOADER_ID_SKILL),
                new ProfileRow("Time", "", "", LOADER_ID_TIME),
                new ProfileRow("Age", "", "", LOADER_ID_NONE),
                new ProfileRow("Voice", "", "", LOADER_ID_VOICE)
        };
        ROW_INDEX_PLATFORM = 0;
        ROW_INDEX_PLAYING = 1;
        ROW_INDEX_GAMEPLAY = 2;
        ROW_INDEX_COUNTRY = 3;
        ROW_INDEX_LANGUAGE = 4;
        ROW_INDEX_SKILL = 5;
        ROW_INDEX_TIME = 6;
        ROW_INDEX_AGE = 7;
        ROW_INDEX_VOICE = 8;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buddy, container, false);

        mCommentTextView = (TextView) rootView.findViewById(R.id.buddy_comment_text_view);

        mAdapter = new SearchAdapter(getActivity(), R.layout.list_item_search, mData);
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
            long buddyId = getActivity().getIntent().getLongExtra(BUDDY_ID_EXTRA, -1);
            Uri query = BuddyEntry.buildBuddyUri(buddyId);
            return new CursorLoader(
                    getActivity(),
                    query,
                    BuddyColumns,
                    null,
                    null,
                    null);
        } else {
            String idList = null;
            switch (id) {
                case LOADER_ID_PLATFORM:
                    idList = mData[ROW_INDEX_PLATFORM].attributeType;
                    break;
                case LOADER_ID_PLAYING:
                    idList = mData[ROW_INDEX_PLAYING].attributeType;
                    break;
                case LOADER_ID_GAMEPLAY:
                    idList = mData[ROW_INDEX_GAMEPLAY].attributeType;
                    break;
                case LOADER_ID_COUNTRY:
                    idList = mData[ROW_INDEX_COUNTRY].attributeType;
                    break;
                case LOADER_ID_LANGUAGE:
                    idList = mData[ROW_INDEX_LANGUAGE].attributeType;
                    break;
                case LOADER_ID_SKILL:
                    idList = mData[ROW_INDEX_SKILL].attributeType;
                    break;
                case LOADER_ID_TIME:
                    idList = mData[ROW_INDEX_TIME].attributeType;
                    break;
                case LOADER_ID_VOICE:
                    idList = mData[ROW_INDEX_VOICE].attributeType;
                    break;
            }
            String select = AttributeEntry._ID + " IN (" + idList + ")";
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
                // we can update our local data structure to hold untransformed data
                mCommentTextView.setText(data.getString(COL_COMMENTS));
                mData[ROW_INDEX_VOICE].value = data.getString(COL_VOICE);
                //
                mData[ROW_INDEX_PLATFORM].attributeType = data.getString(COL_PLATFORM);
                mData[ROW_INDEX_PLAYING].attributeType = data.getString(COL_PLAYING);
                mData[ROW_INDEX_GAMEPLAY].attributeType = data.getString(COL_GAMEPLAY);
                mData[ROW_INDEX_COUNTRY].attributeType = data.getString(COL_COUNTRY);
                mData[ROW_INDEX_LANGUAGE].attributeType = data.getString(COL_LANGUAGE);
                mData[ROW_INDEX_SKILL].attributeType = data.getString(COL_SKILL);
                mData[ROW_INDEX_TIME].attributeType = data.getString(COL_TIME);
                // then kick off the sub-loaders to transform this data
                for (int i = 0; i < mData.length; i++) {
                    if (LOADER_ID_NONE != mData[i].loaderId) {
                        getLoaderManager().initLoader(mData[i].loaderId, null, this);
                    }
                }
            }
        } else {
            // find the correct ProfileRow to update based on the loaderId
            ProfileRow row = null;
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
