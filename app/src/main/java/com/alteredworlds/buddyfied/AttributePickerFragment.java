package com.alteredworlds.buddyfied;

/**
 * Created by twcgilbert on 19/08/2014.
 */

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
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.alteredworlds.buddyfied.data.BuddyfiedContract;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.view_model.AttributePickerAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class AttributePickerFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ATTRIBUTE_LOADER = 1;
    private final String LOG_TAG = AttributePickerFragment.class.getSimpleName();

    public static final String ATTRIBUTE_TYPE_EXTRA = "attribute_type";
    public static final String ATTRIBUTE_DISPLAY_EXTRA = "attribute_display";

    private static final String ATTRIBUTE_TYPE_KEY = "attribute_type";

    private static final String[] ATTRIBUTE_COLUMNS = {
            AttributeEntry.TABLE_NAME + "." + AttributeEntry._ID,
            AttributeEntry.COLUMN_NAME
    };

    // These indices are tied to ATTRIBUTE_COLUMNS.  If ATTRIBUTE_COLUMNS changes, these
    // must change.
    public static final int COL_ATTRIBUTE_ID = 0;
    public static final int COL_ATTRIBUTE_NAME = 1;
    public static final int COL_ATTRIBUTE_IN_PROFILE = 2;

    private String mAttributeType;
    private AttributePickerAdapter mCursorAdapter;

    public AttributePickerFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null != savedInstanceState) {
            mAttributeType = savedInstanceState.getString(ATTRIBUTE_TYPE_KEY);
        }
        getLoaderManager().initLoader(ATTRIBUTE_LOADER, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((null != mAttributeType) &&
                !mAttributeType.equals(getActivity().getIntent().getStringExtra(ATTRIBUTE_TYPE_EXTRA)))
        {
            getLoaderManager().restartLoader(ATTRIBUTE_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_attribute_picker, container, false);

        Intent intent = getActivity().getIntent();
        String title = intent.getStringExtra(ATTRIBUTE_DISPLAY_EXTRA);

        mCursorAdapter = new AttributePickerAdapter(getActivity(), null, 0);

        final ListView listView = (ListView) rootView.findViewById(R.id.listview_attribute_picker);
        listView.setAdapter(mCursorAdapter);
        listView.setItemsCanFocus(false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView ctv = (CheckedTextView) view.findViewById(R.id.list_item_attribute_value);
                boolean setCheckedTo = !ctv.isChecked();
                ctv.setChecked(setCheckedTo);
            }
        });

        getActivity().setTitle(title);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ATTRIBUTE_TYPE_KEY, mAttributeType);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        mAttributeType = intent.getStringExtra(ATTRIBUTE_TYPE_EXTRA);
        //Uri query = BuddyfiedContract.AttributeEntry.buildAttributeType(mAttributeType);
        Uri query = BuddyfiedContract.AttributeEntry.buildAttributeTypeForProfileAll(mAttributeType, 0l);
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                query,
                ATTRIBUTE_COLUMNS,
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
