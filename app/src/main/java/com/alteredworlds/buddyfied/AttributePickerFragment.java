package com.alteredworlds.buddyfied;

/**
 * Created by twcgilbert on 19/08/2014.
 */

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alteredworlds.buddyfied.data.BuddyfiedContract;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeEntry;
import com.alteredworlds.buddyfied.view_model.AttributePickerAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class AttributePickerFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ATTRIBUTE_LOADER = 1;
    private final String LOG_TAG = AttributePickerFragment.class.getSimpleName();

    public static final String PROFILE_ID_EXTRA = "profile_id";
    public static final String ATTRIBUTE_TYPE_EXTRA = "attribute_type";
    public static final String ATTRIBUTE_DISPLAY_EXTRA = "attribute_display";
    public static final String ATTIBUTE_SINGLE_CHOICE_EXTRA = "attribute_single_choice";

    public static final int COL_ATTRIBUTE_ID = 0;
    public static final int COL_ATTRIBUTE_NAME = 1;
    public static final int COL_ATTRIBUTE_IN_PROFILE = 2;

    private static final int NO_ROW_CHECKED = -1;
    private String mAttributeType;
    private AttributePickerAdapter mCursorAdapter;
    private int mProfileId;
    private String mTitle;
    private Uri mQuery;
    private int mLastCheckedPosition;

    public AttributePickerFragment() {
    }

    public void setLastCheckedPosition(int value) {
        mLastCheckedPosition = value;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null != savedInstanceState) {
            mAttributeType = savedInstanceState.getString(ATTRIBUTE_TYPE_EXTRA);
            mProfileId = savedInstanceState.getInt(PROFILE_ID_EXTRA);
            mTitle = savedInstanceState.getString(ATTRIBUTE_DISPLAY_EXTRA);
        }
        else {
            Intent intent = getActivity().getIntent();
            mAttributeType = intent.getStringExtra(ATTRIBUTE_TYPE_EXTRA);
            mProfileId = intent.getIntExtra(PROFILE_ID_EXTRA, 0);
            mTitle = intent.getStringExtra(ATTRIBUTE_DISPLAY_EXTRA);
        }
        mQuery = BuddyfiedContract.AttributeEntry.buildAttributeTypeForProfileAll(mAttributeType, mProfileId);
        getLoaderManager().initLoader(ATTRIBUTE_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ATTRIBUTE_TYPE_EXTRA, mAttributeType);
        outState.putInt(PROFILE_ID_EXTRA, mProfileId);
        outState.putString(ATTRIBUTE_DISPLAY_EXTRA, mTitle);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(ATTRIBUTE_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_attribute_picker, container, false);

        mCursorAdapter = new AttributePickerAdapter(getActivity(), null, 0, this);
        mLastCheckedPosition = NO_ROW_CHECKED;

        final ListView listView = (ListView) rootView.findViewById(R.id.listview_attribute_picker);
        listView.setAdapter(mCursorAdapter);
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(getActivity().getIntent().getBooleanExtra(ATTIBUTE_SINGLE_CHOICE_EXTRA, false) ?
                ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listView.getChoiceMode() == ListView.CHOICE_MODE_SINGLE) {
                    if ((NO_ROW_CHECKED != mLastCheckedPosition) && (position != mLastCheckedPosition)) {
                        // we need to explicitly UN-CHECK this last checked item
                        associateAttribute(mLastCheckedPosition, false);
                    }
                }
                Boolean isChecked = listView.isItemChecked(position);
                associateAttribute(position, isChecked);
                mLastCheckedPosition = isChecked ? position : NO_ROW_CHECKED;
//
//                // if the search has changed, we need to remove all Buddies
//                Intent intent = new Intent(getActivity(), BuddyQueryService.class);
//                intent.putExtra(BuddyQueryService.METHOD_EXTRA, BuddyQueryService.DeleteBuddies);
//                getActivity().startService(intent);
            }
        });

        getActivity().setTitle(mTitle);
        return rootView;
    }

    protected void associateAttribute(int position, boolean add) {
        Cursor cursor = (Cursor)mCursorAdapter.getItem(position);
        long attributeId = cursor.getLong(COL_ATTRIBUTE_ID);
        if (add) {
            ContentValues cv = new ContentValues();
            cv.put(ProfileAttributeEntry.COLUMN_ATTRIBUTE_ID, attributeId);
            cv.put(ProfileAttributeEntry.COLUMN_PROFILE_ID, mProfileId);
            getActivity().getContentResolver().insert(ProfileAttributeEntry.CONTENT_URI, cv);
        }
        else {
            getActivity().getContentResolver().delete(
                    ProfileAttributeEntry.CONTENT_URI,
                    ProfileAttributeEntry.COLUMN_ATTRIBUTE_ID + " = ? AND " +
                            ProfileAttributeEntry.COLUMN_PROFILE_ID + " = ?",
                    new String[] {String.valueOf(attributeId), String.valueOf(mProfileId)});
        }
        getActivity().getContentResolver().notifyChange(mQuery, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                mQuery,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished");
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoaderReset");
        mCursorAdapter.swapCursor(null);
    }
}
