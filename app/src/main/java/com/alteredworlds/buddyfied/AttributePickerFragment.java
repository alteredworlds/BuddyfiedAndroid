package com.alteredworlds.buddyfied;

/**
 * Created by twcgilbert on 19/08/2014.
 */

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeEntry;
import com.alteredworlds.buddyfied.service.BuddyQueryService;
import com.alteredworlds.buddyfied.service.BuddySearchService;
import com.alteredworlds.buddyfied.view_model.AttributePickerAdapter;
import com.alteredworlds.buddyfied.view_model.LoaderID;

public class AttributePickerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = AttributePickerFragment.class.getSimpleName();

    public static final String PROFILE_ID_EXTRA = "profile_id";
    public static final String ATTRIBUTE_TYPE_EXTRA = "attribute_type";
    public static final String ATTRIBUTE_DISPLAY_EXTRA = "attribute_display";
    public static final String ATTIBUTE_SINGLE_CHOICE_EXTRA = "attribute_single_choice";

    public static final String[] ATTRIBS_FILTERED_COLUMNS = {
            AttributeEntry.TABLE_NAME + "." + AttributeEntry._ID,
            AttributeEntry.TABLE_NAME + "." + AttributeEntry.COLUMN_NAME
    };

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
    private EditText mFilterEditText;
    private String mFilterString;
    private Boolean mShowOnlyCheckedItems = false;

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
        } else {
            Intent intent = getActivity().getIntent();
            mAttributeType = intent.getStringExtra(ATTRIBUTE_TYPE_EXTRA);
            mProfileId = intent.getIntExtra(PROFILE_ID_EXTRA, 0);
        }
        getLoaderManager().initLoader(LoaderID.ATTRIBUTE, null, this);
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
        getLoaderManager().initLoader(LoaderID.ATTRIBUTE, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_attribute_picker, container, false);
        setHasOptionsMenu(true);

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
                if ((ListView.CHOICE_MODE_SINGLE == listView.getChoiceMode()) &&
                        (NO_ROW_CHECKED != mLastCheckedPosition)) {
                    if (position == mLastCheckedPosition) {
                        // user clicked on checked row, meaning to UN-CHECK it
                        listView.setItemChecked(position, false);
                    } else {
                        // make sure our model UN-CHECKs the previous item
                        associateAttribute(mLastCheckedPosition, false);
                    }
                }
                Boolean isChecked = listView.isItemChecked(position);
                associateAttribute(position, isChecked);
                mLastCheckedPosition = isChecked ? position : NO_ROW_CHECKED;

                // if the search has changed, we need to:
                // stop any running search
                Intent intent = new Intent(getActivity(), BuddySearchService.class);
                intent.putExtra(BuddySearchService.METHOD_EXTRA, BuddySearchService.Cancel);
                getActivity().startService(intent);
                //
                // remove all Buddies
                intent = new Intent(getActivity(), BuddyQueryService.class);
                intent.putExtra(BuddyQueryService.METHOD_EXTRA, BuddyQueryService.DeleteBuddies);
                getActivity().startService(intent);
            }
        });

        getActivity().setTitle(getActivity().getIntent().getStringExtra(ATTRIBUTE_DISPLAY_EXTRA));
        //
        mFilterEditText = (EditText) rootView.findViewById(R.id.edittext_attribute_picker);
        if (pickerShouldDisplaySearch(getActivity().getIntent().getStringExtra(ATTRIBUTE_TYPE_EXTRA))) {
            listView.setFastScrollEnabled(true);
            listView.setTextFilterEnabled(true);
            mFilterEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mFilterString = s.toString();
                    getLoaderManager().restartLoader(LoaderID.ATTRIBUTE, null, AttributePickerFragment.this);
                }
            });
            mFilterEditText.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        mFilterEditText.setCursorVisible(true);
                    } else {
                        mFilterEditText.setCursorVisible(false);
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mFilterEditText.getWindowToken(), 0);
                    }
                }
            });
            mFilterEditText.clearFocus();
            getActivity().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        } else {
            // get rid of the filter EditText
            mFilterEditText.setVisibility(View.GONE);
        }
        return rootView;
    }

    public void clearEditFocus(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mFilterEditText.isFocused()) {
                Rect outRect = new Rect();
                mFilterEditText.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    mFilterEditText.clearFocus();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.attribute_picker_filter) {
            // do the refresh
            toggleCheckedFilter();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleCheckedFilter() {
        mShowOnlyCheckedItems = !mShowOnlyCheckedItems;
        getLoaderManager().restartLoader(LoaderID.ATTRIBUTE, null, this);
    }


    protected void associateAttribute(int position, boolean add) {
        Cursor cursor = (Cursor) mCursorAdapter.getItem(position);
        long attributeId = cursor.getLong(COL_ATTRIBUTE_ID);
        if (add) {
            ContentValues cv = new ContentValues();
            cv.put(ProfileAttributeEntry.COLUMN_ATTRIBUTE_ID, attributeId);
            cv.put(ProfileAttributeEntry.COLUMN_PROFILE_ID, mProfileId);
            getActivity().getContentResolver().insert(ProfileAttributeEntry.CONTENT_URI, cv);
        } else {
            getActivity().getContentResolver().delete(
                    ProfileAttributeEntry.CONTENT_URI,
                    ProfileAttributeEntry.COLUMN_ATTRIBUTE_ID + " = ? AND " +
                            ProfileAttributeEntry.COLUMN_PROFILE_ID + " = ?",
                    new String[]{String.valueOf(attributeId), String.valueOf(mProfileId)});
        }
        getActivity().getContentResolver().notifyChange(mQuery, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        if ((null != mFilterString) && (mFilterString.length() > 0)) {
            selection = AttributeEntry.TABLE_NAME + "." + AttributeEntry.COLUMN_NAME +
                    " LIKE '%" + mFilterString + "%'";
        }
        mQuery = mShowOnlyCheckedItems ?
                AttributeEntry.buildAttributeTypeForProfile(mAttributeType, mProfileId) :
                AttributeEntry.buildAttributeTypeForProfileAll(mAttributeType, mProfileId);
        // WARN: Buddyfied[Content]Provider supports extremely limited options
        // here - could be fixed up, but limited time right now.
        // only supports selection (NOT selectionArgs)
        return new CursorLoader(
                getActivity(),
                mQuery,
                ATTRIBS_FILTERED_COLUMNS,
                selection,
                null,
                AttributeEntry.TABLE_NAME + "." + AttributeEntry.COLUMN_NAME
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

    private Boolean pickerShouldDisplaySearch(String attributeType) {
        return (0 == AttributeEntry.TypePlaying.compareTo(attributeType)) ||
                (0 == AttributeEntry.TypeCountry.compareTo(attributeType)) ||
                (0 == AttributeEntry.TypeLanguage.compareTo(attributeType));
    }
}
