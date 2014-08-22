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
import com.alteredworlds.buddyfied.view_model.ProfileRow;
import com.alteredworlds.buddyfied.view_model.SearchAdapter;


/**
 * Created by twcgilbert on 30/07/2014.
 */
public class SearchFragment extends Fragment   implements LoaderManager.LoaderCallbacks<Cursor> {

    protected int mProfileId;
    protected ProfileRow[] mData;
    protected SearchAdapter mAdapter;

    private static final int LOADER_ID_PLATFORM = 0;
    private static final int LOADER_ID_PLAYING = 1;
    private static final int LOADER_ID_GAMEPLAY = 2;
    private static final int LOADER_ID_COUNTRY = 3;
    private static final int LOADER_ID_LANGUAGE = 4;
    private static final int LOADER_ID_SKILL = 5;
    private static final int LOADER_ID_TIME = 6;
    private static final int LOADER_ID_AGERANGE = 7;
    private static final int LOADER_ID_VOICE = 8;

    public SearchFragment() {
        mData = new ProfileRow[] {
                new ProfileRow("Platform", "", AttributeEntry.TypePlatform, LOADER_ID_PLATFORM),
                new ProfileRow("Playing", "", AttributeEntry.TypePlaying, LOADER_ID_PLAYING),
                new ProfileRow("Gameplay", "", AttributeEntry.TypeGameplay, LOADER_ID_GAMEPLAY),
                new ProfileRow("Country", "", AttributeEntry.TypeCountry, LOADER_ID_COUNTRY),
                new ProfileRow("Language", "", AttributeEntry.TypeLanguage, LOADER_ID_LANGUAGE),
                new ProfileRow("Skill", "", AttributeEntry.TypeSkill, LOADER_ID_SKILL),
                new ProfileRow("Time", "", AttributeEntry.TypeTime, LOADER_ID_TIME),
                new ProfileRow("Age", "", AttributeEntry.TypeAgeRange, LOADER_ID_AGERANGE),
                new ProfileRow("Voice", "", AttributeEntry.TypeVoice, LOADER_ID_VOICE)
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mProfileId = ((MainActivity) getActivity()).getSearchProfileId();
        for (int i = 0; i < mData.length; i++) {
            getLoaderManager().initLoader(mData[i].loaderId, null, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        for (int i = 0; i < mData.length; i++) {
            getLoaderManager().restartLoader(mData[i].loaderId, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        mAdapter = new SearchAdapter(getActivity(), R.layout.list_item_search, mData);
        ListView listView = (ListView)rootView.findViewById(R.id.listview_search);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProfileRow row = mAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), AttributePickerActivity.class);
                intent.putExtra(AttributePickerFragment.PROFILE_ID_EXTRA, mProfileId);
                intent.putExtra(AttributePickerFragment.ATTRIBUTE_TYPE_EXTRA, row.attributeType);
                intent.putExtra(AttributePickerFragment.ATTRIBUTE_DISPLAY_EXTRA, row.name);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String attributeType = null;
        switch (id) {
            case LOADER_ID_PLATFORM:
                attributeType = AttributeEntry.TypePlatform;
                break;
            case LOADER_ID_PLAYING:
                attributeType = AttributeEntry.TypePlaying;
                break;
            case LOADER_ID_GAMEPLAY:
                attributeType = AttributeEntry.TypeGameplay;
                break;
            case LOADER_ID_COUNTRY:
                attributeType = AttributeEntry.TypeCountry;
                break;
            case LOADER_ID_LANGUAGE:
                attributeType = AttributeEntry.TypeLanguage;
                break;
            case LOADER_ID_SKILL:
                attributeType = AttributeEntry.TypeSkill;
                break;
            case LOADER_ID_TIME:
                attributeType = AttributeEntry.TypeTime;
                break;
            case LOADER_ID_AGERANGE:
                attributeType = AttributeEntry.TypeAgeRange;
                break;
            case LOADER_ID_VOICE:
                attributeType = AttributeEntry.TypeVoice;
                break;
        }
        Uri query = AttributeEntry.buildAttributeTypeForProfile(attributeType, mProfileId);
        return new CursorLoader(
                getActivity(),
                query,
                new String[] {AttributeEntry.COLUMN_NAME},
                null,
                null,
                AttributeEntry.COLUMN_NAME + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        // find the correct ProfileRow to update based on the loaderId
        ProfileRow row = null;
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
