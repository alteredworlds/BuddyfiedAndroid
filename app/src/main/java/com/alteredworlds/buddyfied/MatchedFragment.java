package com.alteredworlds.buddyfied;

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
import android.widget.GridView;

import com.alteredworlds.buddyfied.data.BuddyfiedContract.BuddyEntry;
import com.alteredworlds.buddyfied.service.BuddyQueryService;
import com.alteredworlds.buddyfied.view_model.MatchedAdapter;

/**
 * Created by twcgilbert on 30/07/2014.
 */
public class MatchedFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MatchedFragment.class.getSimpleName();

    private static final int MATCHED_LOADER = 0;

    public static final String[] BuddyColumns = {
            BuddyEntry._ID,
            BuddyEntry.COLUMN_NAME,
            BuddyEntry.COLUMN_IMAGE_URI
    };

    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_IMAGE_URI = 2;

    private Uri mQuery;
    private MatchedAdapter mMatchedAdaptor;

    public MatchedFragment() {
        mQuery = BuddyEntry.CONTENT_URI;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MATCHED_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_matched, container, false);

        mMatchedAdaptor = new MatchedAdapter(getActivity(), null, 0);

        final GridView gridView = (GridView) rootView.findViewById(R.id.matched_gridview);
        gridView.setAdapter(mMatchedAdaptor);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) mMatchedAdaptor.getItem(position);
                Intent intent = new Intent(getActivity(), BuddyActivity.class);
                intent.putExtra(BuddyFragment.BUDDY_ID_EXTRA, cursor.getLong(COL_ID));
                intent.putExtra(BuddyFragment.BUDDY_NAME_EXTRA, cursor.getString(COL_NAME));
                intent.putExtra(BuddyFragment.BUDDY_IMAGE_URI_EXTRA, cursor.getString(COL_IMAGE_URI));
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // this code is ALWAYS QUERYING
        Intent intent = new Intent(getActivity(), BuddyQueryService.class);
        intent.putExtra(BuddyQueryService.METHOD_EXTRA, BuddyQueryService.GetMatches);
        intent.putExtra(BuddyQueryService.PROFILE_ID_EXTRA, ((MainActivity) getActivity()).getSearchProfileId());
        getActivity().startService(intent);
        // starts the loader to view existing data
        //getLoaderManager().restartLoader(MATCHED_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                mQuery,
                MatchedFragment.BuddyColumns,
                null,
                null,
                BuddyEntry.COLUMN_DISPLAY_ORDER
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished");
        mMatchedAdaptor.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoaderReset");
        mMatchedAdaptor.swapCursor(null);
    }
}