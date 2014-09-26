package com.alteredworlds.buddyfied;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alteredworlds.buddyfied.data.BuddyfiedContract;
import com.alteredworlds.buddyfied.service.BuddyUserService;
import com.alteredworlds.buddyfied.view_model.BuddyHeaderListItem;
import com.alteredworlds.buddyfied.view_model.CommentsListItem;
import com.alteredworlds.buddyfied.view_model.LoaderID;
import com.alteredworlds.buddyfied.view_model.LoaderListItem;
import com.alteredworlds.buddyfied.view_model.ProfileListItem;
import com.alteredworlds.buddyfied.view_model.SearchListItem;

/**
 * Created by twcgilbert on 26/09/2014.
 */
public class ProfileFragmentMaleable extends ProfileFragmentBase {

    protected Boolean mDisableNextButton = false;
    protected BroadcastReceiver mMessageReceiver;
    protected ProgressBar mProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mData = new LoaderListItem[]{
                new BuddyHeaderListItem("", "", true),
                new ProfileListItem("Platform", "", BuddyfiedContract.AttributeEntry.TypePlatform, LoaderID.PROFILE_PLATFORM),
                new ProfileListItem("Playing", "", BuddyfiedContract.AttributeEntry.TypePlaying, LoaderID.PROFILE_PLAYING),
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

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.profile_progress);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_profile);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // move to relevant editor for given row
                if (HEADER_ROW == position) {

                } else if (AGE_ROW == position) {
                    Intent intent = new Intent(getActivity(), AgeSelectionActivity.class);
                    intent.putExtra(Constants.ID_EXTRA, mProfileId);
                    intent.putExtra(AgeSelectionActivity.AGE_EXTRA, mData[position].value);
                    startActivity(intent);
                } else if (COMMENTS_ROW == position) {
                    Intent intent = new Intent(getActivity(), CommentEditorActivity.class);
                    intent.putExtra(Constants.ID_EXTRA, mProfileId);
                    startActivity(intent);
                } else {
                    LoaderListItem row = (LoaderListItem) mAdapter.getItem(position);
                    Intent intent = new Intent(getActivity(), AttributePickerActivity.class);
                    intent.putExtra(AttributePickerFragment.PROFILE_ID_EXTRA, mProfileId);
                    intent.putExtra(AttributePickerFragment.ATTRIBUTE_TYPE_EXTRA, row.extra);
                    intent.putExtra(AttributePickerFragment.ATTRIBUTE_DISPLAY_EXTRA, row.name);
                    intent.putExtra(AttributePickerFragment.ATTIBUTE_SINGLE_CHOICE_EXTRA,
                            (0 == BuddyfiedContract.AttributeEntry.TypeVoice.compareTo(row.extra)));
                    startActivity(intent);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //
        // Register an observer to receive specific named Intents ('events')
        if (null != mMessageReceiver) {
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                    new IntentFilter(BuddyUserService.BUDDY_USER_SERVICE_RESULT_EVENT));
        }
    }

    @Override
    public void onPause() {
        // cancel any active communications relating to profile (register/amend)
        if (!getActivity().isChangingConfigurations()) {
            // should allow call to continue during device rotation
            Intent intent = new Intent(getActivity(), BuddyUserService.class);
            intent.putExtra(Constants.METHOD_EXTRA, BuddyUserService.CANCEL);
            getActivity().startService(intent);
        }
        //
        // Unregister since the activity is about to be closed.
        if (null != mMessageReceiver) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        }
        super.onPause();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem nextMenuItem = menu.findItem(R.id.action_next);
        if (null != nextMenuItem) {
            nextMenuItem.setEnabled(!mDisableNextButton);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile_fragment_maleable, menu);
    }

    protected void showProgress(Boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    protected Boolean validateProfile() {
        Boolean retVal = true;
        for (LoaderListItem lli : mData) {
            if (lli instanceof ProfileListItem) {
                retVal &= !TextUtils.isEmpty(lli.value);
                if (!retVal) {
                    String description = lli.name + " " + getString(R.string.supply_value_for_required_field);
                    showMessage(description);
                    break;
                }
            }
        }
        return retVal;
    }

    protected void setNextButtonEnabled(Boolean enabled) {
        mDisableNextButton = !enabled;
        getActivity().invalidateOptionsMenu();
    }

    protected void showMessage(String message) {
        showMessage(message, false);
    }

    protected void showMessage(String message, Boolean longTime) {
        if (!TextUtils.isEmpty(message)) {
            Toast toast = Toast.makeText(getActivity(), message, longTime ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}
