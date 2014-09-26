package com.alteredworlds.buddyfied;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.alteredworlds.buddyfied.data.BuddyfiedContract;
import com.alteredworlds.buddyfied.service.BuddyQueryService;
import com.alteredworlds.buddyfied.view_model.BuddyHeaderListItem;
import com.alteredworlds.buddyfied.view_model.CommentsListItem;
import com.alteredworlds.buddyfied.view_model.LoaderID;
import com.alteredworlds.buddyfied.view_model.LoaderListItem;
import com.alteredworlds.buddyfied.view_model.SearchListItem;

/**
 * Created by twcgilbert on 26/09/2014.
 */
public class ProfileFragmentReadonly extends ProfileFragmentBase {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    new SearchListItem("Platform", "", BuddyfiedContract.AttributeEntry.TypePlatform, LoaderID.PROFILE_PLATFORM),
                    new SearchListItem("Playing", "", BuddyfiedContract.AttributeEntry.TypePlaying, LoaderID.PROFILE_PLAYING),
                    new SearchListItem("Gameplay", "", BuddyfiedContract.AttributeEntry.TypeGameplay, LoaderID.PROFILE_GAMEPLAY),
                    new SearchListItem("Country", "", BuddyfiedContract.AttributeEntry.TypeCountry, LoaderID.PROFILE_COUNTRY),
                    new SearchListItem("Language", "", BuddyfiedContract.AttributeEntry.TypeLanguage, LoaderID.PROFILE_LANGUAGE),
                    new SearchListItem("Skill", "", BuddyfiedContract.AttributeEntry.TypeSkill, LoaderID.PROFILE_SKILL),
                    new SearchListItem("Time", "", BuddyfiedContract.AttributeEntry.TypeTime, LoaderID.PROFILE_TIME),
                    new SearchListItem("Age", "", BuddyfiedContract.AttributeEntry.TypeAgeRange, LoaderID.NONE),
                    new SearchListItem("Voice", "", BuddyfiedContract.AttributeEntry.TypeVoice, LoaderID.PROFILE_VOICE),
                    new CommentsListItem("Comments", "", "")
            };
            HEADER_ROW = 0;
            AGE_ROW = 8;
            COMMENTS_ROW = 10;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), BuddyQueryService.class);
        intent.putExtra(Constants.METHOD_EXTRA, BuddyQueryService.GetMemberInfo);
        intent.putExtra(Constants.ID_EXTRA, mProfileId);
        getActivity().startService(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!Settings.isGuestUser(getActivity())) {
            inflater.inflate(R.menu.profile_fragment, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                onEdit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onEdit() {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(Constants.ID_EXTRA, mProfileId);
        startActivity(intent);
    }
}