package com.alteredworlds.buddyfied;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alteredworlds.buddyfied.data.BuddyfiedContract.BuddyEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedDbHelper;
import com.alteredworlds.buddyfied.service.BuddySearchService;
import com.alteredworlds.buddyfied.view_model.LoaderID;
import com.alteredworlds.buddyfied.view_model.MatchedAdapter;

/**
 * Created by twcgilbert on 30/07/2014.
 */
public class MatchedFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MatchedFragment.class.getSimpleName();

    public static final String[] BuddyColumns = {
            BuddyEntry._ID,
            BuddyEntry.COLUMN_NAME,
            BuddyEntry.COLUMN_COMMENTS,
            BuddyEntry.COLUMN_IMAGE_URI,
            BuddyEntry.COLUMN_AGE,
            BuddyEntry.COLUMN_COUNTRY,
            BuddyEntry.COLUMN_GAMEPLAY,
            BuddyEntry.COLUMN_LANGUAGE,
            BuddyEntry.COLUMN_PLATFORM,
            BuddyEntry.COLUMN_PLAYING,
            BuddyEntry.COLUMN_SKILL,
            BuddyEntry.COLUMN_TIME,
            BuddyEntry.COLUMN_VOICE
    };

    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_COMMENTS = 2;
    public static final int COL_IMAGE_URI = 3;
    public static final int COL_AGE = 4;
    public static final int COL_COUNTRY = 5;
    public static final int COL_GAMEPLAY = 6;
    public static final int COL_LANGUAGE = 7;
    public static final int COL_PLATFORM = 8;
    public static final int COL_PLAYING = 9;
    public static final int COL_SKILL = 10;
    public static final int COL_TIME = 11;
    public static final int COL_VOICE = 12;

    private Uri mQuery;
    private MatchedAdapter mMatchedAdaptor;
    private BroadcastReceiver mMessageReceiver;
    private TextView mMatchedText;
    private ProgressBar mProgressBar;
    private GridView mGridView;

    public MatchedFragment() {
        mQuery = BuddyEntry.CONTENT_URI;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_matched, container, false);

        mMatchedText = (TextView) rootView.findViewById(R.id.matched_text);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.matched_progress);

        mMatchedAdaptor = new MatchedAdapter(getActivity(), null, 0);

        mGridView = (GridView) rootView.findViewById(R.id.matched_gridview);
        mGridView.setAdapter(mMatchedAdaptor);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) mMatchedAdaptor.getItem(position);
                Intent intent = new Intent(getActivity(), BuddyActivity.class);
                intent.putExtra(BuddyFragment.BUDDY_ID_EXTRA, cursor.getLong(COL_ID));
                intent.putExtra(BuddyFragment.BUDDY_NAME_EXTRA, cursor.getString(COL_NAME));
                intent.putExtra(BuddyFragment.BUDDY_IMAGE_URI_EXTRA, cursor.getString(COL_IMAGE_URI));
                //
                intent.putExtra(BuddyFragment.BUDDY_VOICE_EXTRA, cursor.getString(COL_VOICE));
                intent.putExtra(BuddyFragment.BUDDY_AGE_EXTRA, cursor.getString(COL_AGE));
                intent.putExtra(BuddyFragment.BUDDY_COMMENTS_EXTRA, cursor.getString(COL_COMMENTS));
                //
                // data that must be transformed via a loader
                intent.putExtra(BuddyFragment.BUDDY_PLATFORM_EXTRA, cursor.getString(COL_PLATFORM));
                intent.putExtra(BuddyFragment.BUDDY_PLAYING_EXTRA, cursor.getString(COL_PLAYING));
                intent.putExtra(BuddyFragment.BUDDY_GAMEPLAY_EXTRA, cursor.getString(COL_GAMEPLAY));
                intent.putExtra(BuddyFragment.BUDDY_COUNTRY_EXTRA, cursor.getString(COL_COUNTRY));
                intent.putExtra(BuddyFragment.BUDDY_LANGUAGE_EXTRA, cursor.getString(COL_LANGUAGE));
                intent.putExtra(BuddyFragment.BUDDY_SKILL_EXTRA, cursor.getString(COL_SKILL));
                intent.putExtra(BuddyFragment.BUDDY_TIME_EXTRA, cursor.getString(COL_TIME));
                startActivity(intent);
            }
        });

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showProgressIndicator(false);
                Bundle results = intent.getBundleExtra(Constants.RESULT_BUNDLE);
                if (null != results) {
                    // we want a code 0 indicating success.
                    int code = results.getInt(Constants.RESULT_CODE, 0);
                    String description = results.getString(Constants.RESULT_DESCRIPTION, "");
                    if (0 == code) {
                        // server response valid, show any non-empty message
                        showMessage(description);
                    } else {
                        // error case, e.g.: malformed XML.
                        // show standard message 'No matches found' & popup an alert with error
                        showMessage(getString(R.string.no_matches_found));
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.search_failed))
                                .setMessage(description)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue with delete
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }
            }
        };

        return rootView;
    }

    private void showMessage(String message) {
        if (null != mMatchedText) {
            if (Utils.isNullOrEmpty(message)) {
                showTextOrProgress(false, false);
            } else {
                showTextOrProgress(true, false);
                mMatchedText.setText(message);
            }
        }
    }

    private void showTextOrProgress(Boolean text, Boolean progress) {
        if (null != mGridView) {
            mGridView.setVisibility((!text && !progress) ? View.VISIBLE : View.GONE);
        }
        if (null != mProgressBar) {
            mProgressBar.setVisibility(progress ? View.VISIBLE : View.GONE);
        }
        if (null != mMatchedText) {
            mMatchedText.setVisibility(text ? View.VISIBLE : View.GONE);
        }
    }

    private void showProgressIndicator(Boolean show) {
        if (null != mProgressBar) {
            if (show) {
                showTextOrProgress(false, true);
            } else {
                showTextOrProgress(false, false);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //
        // Register an observer to receive specific named Intents ('events')
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(BuddySearchService.BUDDY_SEARCH_SERVICE_RESULT_EVENT));
        //
        getLoaderManager().initLoader(LoaderID.MATCHED, null, this);
        //
        if (!haveBuddiesAlready()) {
            Intent intent = new Intent(getActivity(), BuddySearchService.class);
            intent.putExtra(Constants.METHOD_EXTRA, BuddySearchService.GetMatches);
            intent.putExtra(Constants.ID_EXTRA, BuddyfiedDbHelper.SEARCH_PROFILE_ID);
            getActivity().startService(intent);
            showProgressIndicator(true);
        }
    }

    private Boolean haveBuddiesAlready() {
        Cursor cursor = getActivity().getContentResolver().query(
                BuddyEntry.CONTENT_URI,
                new String[]{BuddyEntry._ID},
                null,
                null,
                null);
        Boolean retVal = cursor.getCount() > 0;
        cursor.close();
        return retVal;
    }

    @Override
    public void onPause() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onPause();
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
        //Log.d(LOG_TAG, "onLoadFinished");
        mMatchedAdaptor.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Log.d(LOG_TAG, "onLoaderReset");
        mMatchedAdaptor.swapCursor(null);
    }
}