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

import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedDbHelper;
import com.alteredworlds.buddyfied.view_model.LoaderListItem;
import com.alteredworlds.buddyfied.view_model.SearchAdapter;
import com.alteredworlds.buddyfied.view_model.SearchListItem;


/**
 * Created by twcgilbert on 30/07/2014.
 */
public class SearchFragment extends Fragment   implements LoaderManager.LoaderCallbacks<Cursor> {

    final protected int mProfileId = BuddyfiedDbHelper.SEARCH_PROFILE_ID;
    protected LoaderListItem[] mData;
    protected SearchAdapter mAdapter;

    public SearchFragment() {
        mData = new SearchListItem[]{
                new SearchListItem("Platform", "", AttributeEntry.TypePlatform, LoaderListItem.LOADER_ID_PLATFORM),
                new SearchListItem("Playing", "", AttributeEntry.TypePlaying, LoaderListItem.LOADER_ID_PLAYING),
                new SearchListItem("Gameplay", "", AttributeEntry.TypeGameplay, LoaderListItem.LOADER_ID_GAMEPLAY),
                new SearchListItem("Country", "", AttributeEntry.TypeCountry, LoaderListItem.LOADER_ID_COUNTRY),
                new SearchListItem("Language", "", AttributeEntry.TypeLanguage, LoaderListItem.LOADER_ID_LANGUAGE),
                new SearchListItem("Skill", "", AttributeEntry.TypeSkill, LoaderListItem.LOADER_ID_SKILL),
                new SearchListItem("Time", "", AttributeEntry.TypeTime, LoaderListItem.LOADER_ID_TIME),
                new SearchListItem("Age", "", AttributeEntry.TypeAgeRange, LoaderListItem.LOADER_ID_AGERANGE),
                new SearchListItem("Voice", "", AttributeEntry.TypeVoice, LoaderListItem.LOADER_ID_VOICE)
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        for (int i = 0; i < mData.length; i++) {
            getLoaderManager().initLoader(mData[i].loaderId, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        mAdapter = new SearchAdapter(getActivity(), mData);
        ListView listView = (ListView)rootView.findViewById(R.id.listview_search);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LoaderListItem row = (LoaderListItem) mAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), AttributePickerActivity.class);
                intent.putExtra(AttributePickerFragment.PROFILE_ID_EXTRA, mProfileId);
                intent.putExtra(AttributePickerFragment.ATTRIBUTE_TYPE_EXTRA, row.extra);
                intent.putExtra(AttributePickerFragment.ATTRIBUTE_DISPLAY_EXTRA, row.name);
                intent.putExtra(AttributePickerFragment.ATTIBUTE_SINGLE_CHOICE_EXTRA, singleChoice(row));
                startActivity(intent);
            }
        });

        return rootView;
    }

    private boolean singleChoice(LoaderListItem row) {
        return (0 == AttributeEntry.TypeAgeRange.compareTo(row.extra)) ||
                (0 == AttributeEntry.TypeVoice.compareTo(row.extra));
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
        if ((null != row) && !Utils.isNullOrEmpty(row.extra)) {
            // we have the AttributeEntry.COLUMN_TYPE information in row.extra
            Uri query = AttributeEntry.buildAttributeTypeForProfile(row.extra, mProfileId);
            retVal = new CursorLoader(
                    getActivity(),
                    query,
                    new String[]{AttributeEntry.COLUMN_NAME},
                    null,
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
        for (int i =0; i < mData.length; i++) {
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
