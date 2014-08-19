package com.alteredworlds.buddyfied;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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
                new ProfileRow("Platform", "Mac"),
                new ProfileRow("Playing", ""),
                new ProfileRow("Gameplay", ""),
                new ProfileRow("Country", ""),
                new ProfileRow("Language", ""),
                new ProfileRow("Skill", ""),
                new ProfileRow("Time", ""),
                new ProfileRow("Age", ""),
                new ProfileRow("Voice", "")
        };

        SearchAdapter adapter = new SearchAdapter(getActivity(), R.layout.list_item_search, sampleData);
        ListView listView = (ListView)rootView.findViewById(R.id.listview_search);
        listView.setAdapter(adapter);

        return rootView;
    }
}
