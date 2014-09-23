package com.alteredworlds.buddyfied;

import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ListView;

import com.alteredworlds.buddyfied.data.BuddyfiedContract;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;
import com.alteredworlds.buddyfied.service.BuddyQueryService;
import com.alteredworlds.buddyfied.view_model.BuddyAdapter;
import com.alteredworlds.buddyfied.view_model.BuddyHeaderListItem;
import com.alteredworlds.buddyfied.view_model.CommentsListItem;
import com.alteredworlds.buddyfied.view_model.LoaderID;
import com.alteredworlds.buddyfied.view_model.LoaderListItem;
import com.alteredworlds.buddyfied.view_model.ProfileListItem;
import com.alteredworlds.buddyfied.view_model.SearchListItem;

/**
 * Created by twcgilbert on 30/07/2014.
 */
public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = ProfileFragment.class.getSimpleName();

    private static final String EDIT_MODE_KEY = "edit_mode";

    private LoaderListItem[] mData;
    private BuddyAdapter mAdapter;
    private long mProfileId;
    private int COMMENTS_ROW;
    private int AGE_ROW;
    private int HEADER_ROW;

    public Boolean mEditMode = false;

    private static final String[] ProfileColumns = {
            ProfileEntry.COLUMN_NAME,
            ProfileEntry.COLUMN_IMAGE_URI,
            ProfileEntry.COLUMN_AGE,
            ProfileEntry.COLUMN_COMMENTS
    };
    private static final int COL_NAME = 0;
    private static final int COL_IMAGE_URI = 1;
    private static final int COL_AGE = 2;
    private static final int COL_COMMENTS = 3;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != savedInstanceState) {
            mEditMode = savedInstanceState.getBoolean(EDIT_MODE_KEY, false);
        }
        if (Settings.isGuestUser(getActivity())) {
            mData = new LoaderListItem[]{
                    new BuddyHeaderListItem("", "", true),
                    new CommentsListItem("Comments", "", "")
            };
            HEADER_ROW = 0;
            AGE_ROW = -1;
            COMMENTS_ROW = 1;
        } else {
            mData = new LoaderListItem[]{
                    new BuddyHeaderListItem("", "", true),
                    new ProfileListItem("Platform", "", AttributeEntry.TypePlatform, LoaderID.PROFILE_PLATFORM),
                    new ProfileListItem("Playing", "", AttributeEntry.TypePlaying, LoaderID.PROFILE_PLAYING),
                    new SearchListItem("Gameplay", "", AttributeEntry.TypeGameplay, LoaderID.PROFILE_GAMEPLAY),
                    new SearchListItem("Country", "", AttributeEntry.TypeCountry, LoaderID.PROFILE_COUNTRY),
                    new SearchListItem("Language", "", AttributeEntry.TypeLanguage, LoaderID.PROFILE_LANGUAGE),
                    new SearchListItem("Skill", "", AttributeEntry.TypeSkill, LoaderID.PROFILE_SKILL),
                    new SearchListItem("Time", "", AttributeEntry.TypeTime, LoaderID.PROFILE_TIME),
                    new SearchListItem("Age", "", AttributeEntry.TypeAgeRange, LoaderID.NONE),
                    new SearchListItem("Voice", "", AttributeEntry.TypeVoice, LoaderID.PROFILE_VOICE),
                    new CommentsListItem("Comments", "", "")
            };
            HEADER_ROW = 0;
            AGE_ROW = 8;
            COMMENTS_ROW = 10;
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        mProfileId = Settings.getUserId(getActivity());
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mAdapter = new BuddyAdapter(getActivity(), mData);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_profile);
        listView.setAdapter(mAdapter);
        if (mEditMode) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // move to relevant editor for given row
                    if (HEADER_ROW == position) {

                    } else if (AGE_ROW == position) {
                        Intent intent = new Intent(getActivity(), AgeSelectionActivity.class);
                        intent.putExtra(Constants.ID_EXTRA, mProfileId);
                        intent.putExtra(AgeSelectionActivity.AGE_EXTRA, mData[position].value);
                        startActivity(intent);
                    } else if (COMMENTS_ROW == position) {
                        Intent intent = new Intent(getActivity(), CommentEditorActivity.class);
                        intent.putExtra(Constants.ID_EXTRA, mProfileId);
                        startActivity(intent);
                    } else {
                        LoaderListItem row = (LoaderListItem) mAdapter.getItem(position);
                        Intent intent = new Intent(getActivity(), AttributePickerActivity.class);
                        intent.putExtra(AttributePickerFragment.PROFILE_ID_EXTRA, mProfileId);
                        intent.putExtra(AttributePickerFragment.ATTRIBUTE_TYPE_EXTRA, row.extra);
                        intent.putExtra(AttributePickerFragment.ATTRIBUTE_DISPLAY_EXTRA, row.name);
                        intent.putExtra(AttributePickerFragment.ATTIBUTE_SINGLE_CHOICE_EXTRA,
                                (0 == AttributeEntry.TypeVoice.compareTo(row.extra)));
                        startActivity(intent);
                    }
                }
            });
        }
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        //
        if (!mEditMode) {
            Intent intent = new Intent(getActivity(), BuddyQueryService.class);
            intent.putExtra(Constants.METHOD_EXTRA, BuddyQueryService.GetMemberInfo);
            intent.putExtra(Constants.ID_EXTRA, Settings.getUserId(getActivity()));
            getActivity().startService(intent);
        }
        // start loaders
        getLoaderManager().initLoader(LoaderID.PROFILE_MAIN, null, this);
        for (int i = 0; i < mData.length; i++) {
            if (LoaderID.NONE != mData[i].loaderId) {
                getLoaderManager().initLoader(mData[i].loaderId, null, this);
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EDIT_MODE_KEY, mEditMode);
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
            if ((null != row) && !Utils.isNullOrEmpty(row.extra)) {
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

    private void setRowName(int row, String value) {
        if ((-1 != row) && (mData.length > row)) {
            mData[row].name = value;
        }
    }

    private void setRowValue(int row, String value) {
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
                setRowValue(COMMENTS_ROW, data.getString(COL_COMMENTS));
                setRowValue(AGE_ROW, data.getString(COL_AGE));
                setRowName(HEADER_ROW, data.getString(COL_NAME));
                setRowValue(HEADER_ROW, data.getString(COL_IMAGE_URI));
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
