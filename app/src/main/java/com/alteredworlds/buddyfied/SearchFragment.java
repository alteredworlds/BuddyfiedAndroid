package com.alteredworlds.buddyfied;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class SearchFragment extends Fragment {

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        ProfileRow[] sampleData = new ProfileRow[] {
                new ProfileRow("Platform", "Mac", AttributeEntry.TypePlatform),
                new ProfileRow("Playing", "", AttributeEntry.TypePlaying),
                new ProfileRow("Gameplay", "", AttributeEntry.TypeGameplay),
                new ProfileRow("Country", "", AttributeEntry.TypeCountry),
                new ProfileRow("Language", "", AttributeEntry.TypeLanguage),
                new ProfileRow("Skill", "", AttributeEntry.TypeSkill),
                new ProfileRow("Time", "", AttributeEntry.TypeTime),
                new ProfileRow("Age", "", AttributeEntry.TypeAgeRange),
                new ProfileRow("Voice", "", AttributeEntry.TypeVoice)
        };

        final SearchAdapter adapter = new SearchAdapter(getActivity(), R.layout.list_item_search, sampleData);
        ListView listView = (ListView)rootView.findViewById(R.id.listview_search);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProfileRow row = adapter.getItem(position);
                // show listview containing the results of querying following URI
                Uri query = AttributeEntry.buildAttributeType(row.attributeType);
                Intent intent = new Intent(getActivity(), AttributePickerActivity.class);
                intent.putExtra(AttributePickerFragment.ATTRIBUTE_TYPE_EXTRA, row.attributeType);
                intent.putExtra(AttributePickerFragment.ATTRIBUTE_DISPLAY_EXTRA, row.name);
                startActivity(intent);
            }
        });

        return rootView;
    }
}
