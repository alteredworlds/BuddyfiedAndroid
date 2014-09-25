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
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alteredworlds.buddyfied.data.BuddyfiedContract;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;
import com.alteredworlds.buddyfied.service.BuddyQueryService;
import com.alteredworlds.buddyfied.service.BuddyUserService;
import com.alteredworlds.buddyfied.view_model.BuddyAdapter;
import com.alteredworlds.buddyfied.view_model.BuddyHeaderListItem;
import com.alteredworlds.buddyfied.view_model.CommentsListItem;
import com.alteredworlds.buddyfied.view_model.LoaderID;
import com.alteredworlds.buddyfied.view_model.LoaderListItem;
import com.alteredworlds.buddyfied.view_model.ProfileListItem;
import com.alteredworlds.buddyfied.view_model.SearchListItem;

/**
 * Created by twcgilbert on 30/07/2014.
 */
public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = ProfileFragment.class.getSimpleName();

    private static final String EDIT_MODE_KEY = "edit_mode";

    private LoaderListItem[] mData;
    private BuddyAdapter mAdapter;
    private long mProfileId;
    private int COMMENTS_ROW;
    private int AGE_ROW;
    private int HEADER_ROW;

    public Boolean mEditMode = false;
    private BroadcastReceiver mMessageReceiver;
    private Boolean mDisableJoinButton = false;

    private static final String[] ProfileColumns = {
            ProfileEntry.COLUMN_NAME,
            ProfileEntry.COLUMN_IMAGE_URI,
            ProfileEntry.COLUMN_AGE,
            ProfileEntry.COLUMN_COMMENTS
    };
    private static final int COL_NAME = 0;
    private static final int COL_IMAGE_URI = 1;
    private static final int COL_AGE = 2;
    private static final int COL_COMMENTS = 3;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != savedInstanceState) {
            mEditMode = savedInstanceState.getBoolean(EDIT_MODE_KEY, false);
        }
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
                    mEditMode ? new ProfileListItem("Platform", "", AttributeEntry.TypePlatform, LoaderID.PROFILE_PLATFORM) :
                            new SearchListItem("Platform", "", AttributeEntry.TypePlatform, LoaderID.PROFILE_PLATFORM),
                    mEditMode ? new ProfileListItem("Playing", "", AttributeEntry.TypePlaying, LoaderID.PROFILE_PLAYING) :
                            new SearchListItem("Playing", "", AttributeEntry.TypePlaying, LoaderID.PROFILE_PLAYING),
                    new SearchListItem("Gameplay", "", AttributeEntry.TypeGameplay, LoaderID.PROFILE_GAMEPLAY),
                    new SearchListItem("Country", "", AttributeEntry.TypeCountry, LoaderID.PROFILE_COUNTRY),
                    new SearchListItem("Language", "", AttributeEntry.TypeLanguage, LoaderID.PROFILE_LANGUAGE),
                    new SearchListItem("Skill", "", AttributeEntry.TypeSkill, LoaderID.PROFILE_SKILL),
                    new SearchListItem("Time", "", AttributeEntry.TypeTime, LoaderID.PROFILE_TIME),
                    new SearchListItem("Age", "", AttributeEntry.TypeAgeRange, LoaderID.NONE),
                    new SearchListItem("Voice", "", AttributeEntry.TypeVoice, LoaderID.PROFILE_VOICE),
                    new CommentsListItem("Comments", "", "")
            };
            HEADER_ROW = 0;
            AGE_ROW = 8;
            COMMENTS_ROW = 10;
        }
        if (mEditMode) {
            mMessageReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Bundle results = intent.getBundleExtra(Constants.RESULT_BUNDLE);
                    if (null != results) {
                        // we want a code 0 indicating success.
                        int code = results.getInt(Constants.RESULT_CODE, Constants.RESULT_OK);
                        String description = results.getString(Constants.RESULT_DESCRIPTION, "");
                        if (Constants.RESULT_OK == code) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.welcome_title) + " " + Settings.getUsername(getActivity()) + "!")
                                    .setMessage(getString(R.string.welcome_message))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            AboutFragment.signOut(getActivity());
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        } else {
                            // error case & popup an alert with error
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.user_registration_failed))
                                    .setMessage(description)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                            // re-enable the JOIN button to allow option of having another go
                            setJoinButtonEnabled(true);
                        }
                    }
                }
            };
        }
    }


    private void showMessage(String message) {
        showMessage(message, false);
    }

    private void showMessage(String message, Boolean longTime) {
        if (!TextUtils.isEmpty(message)) {
            Toast toast = Toast.makeText(getActivity(), message, longTime ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mProfileId = Settings.getUserId(getActivity());
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mAdapter = new BuddyAdapter(getActivity(), mData);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_profile);
        listView.setAdapter(mAdapter);
        if (mEditMode) {
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
                                (0 == AttributeEntry.TypeVoice.compareTo(row.extra)));
                        startActivity(intent);
                    }
                }
            });
        }
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        //
        if (mEditMode) {
            if (null != mMessageReceiver) {
                // Register an observer to receive specific named Intents ('events')
                LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                        new IntentFilter(BuddyUserService.BUDDY_USER_SERVICE_RESULT_EVENT));
            }
        } else {
            Intent intent = new Intent(getActivity(), BuddyQueryService.class);
            intent.putExtra(Constants.METHOD_EXTRA, BuddyQueryService.GetMemberInfo);
            intent.putExtra(Constants.ID_EXTRA, Settings.getUserId(getActivity()));
            getActivity().startService(intent);
        }
        // start loaders
        getLoaderManager().initLoader(LoaderID.PROFILE_MAIN, null, this);
        for (int i = 0; i < mData.length; i++) {
            if (LoaderID.NONE != mData[i].loaderId) {
                getLoaderManager().initLoader(mData[i].loaderId, null, this);
            }
        }
    }

    @Override
    public void onPause() {
        if (mEditMode) {
            // cancel any active communications relating to profile (register/amend)
            Intent intent = new Intent(getActivity(), BuddyUserService.class);
            intent.putExtra(Constants.METHOD_EXTRA, BuddyUserService.CANCEL);
            getActivity().startService(intent);
            //
            // Unregister since the activity is about to be closed.
            if (null != mMessageReceiver) {
                LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
            }
        }
        super.onPause();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EDIT_MODE_KEY, mEditMode);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem joinMenuItem = menu.findItem(R.id.action_join);
        if (null != joinMenuItem) {
            joinMenuItem.setEnabled(!mDisableJoinButton);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_join:
                onJoin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setJoinButtonEnabled(Boolean enabled) {
        mDisableJoinButton = !enabled;
        getActivity().invalidateOptionsMenu();
    }

    private void onJoin() {
        if (validateProfileForSignUp()) {
            setJoinButtonEnabled(false);
            getActivity().invalidateOptionsMenu();
            LayoutInflater li = LayoutInflater.from(getActivity());
            View promptsView = li.inflate(R.layout.eula_prompt, null);
            TextView tv = (TextView) promptsView.findViewById(R.id.eula_link_textview);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            new AlertDialog.Builder(
                    getActivity())
                    .setView(promptsView)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Log.i(LOG_TAG, "Fucking JOIN already...!");
                                    Intent intent = new Intent(getActivity(), BuddyUserService.class);
                                    intent.putExtra(Constants.METHOD_EXTRA, BuddyUserService.REGISTER);
                                    intent.putExtra(Constants.ID_EXTRA, mProfileId);
                                    getActivity().startService(intent);
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    setJoinButtonEnabled(true);
                                }
                            })
                    .create()
                    .show();
        }
    }

    private Boolean validateProfileForSignUp() {
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> retVal = null;
        if (LoaderID.PROFILE_MAIN == id) {
            Uri query = BuddyfiedContract.ProfileEntry.buildProfileUri(mProfileId);
            retVal = new CursorLoader(
                    getActivity(),
                    query,
                    ProfileColumns,
                    null,
                    null,
                    null);

        } else {
            // find the row information for this loader id
            LoaderListItem row = null;
            for (int i = 0; i < mData.length; i++) {
                if (mData[i].loaderId == id) {
                    row = mData[i];
                    break;
                }
            }
            if ((null != row) && !TextUtils.isEmpty(row.extra)) {
                // we have the AttributeEntry.COLUMN_TYPE information in row.extra
                Uri query = BuddyfiedContract.AttributeEntry.buildAttributeTypeForProfile(row.extra, mProfileId);
                retVal = new CursorLoader(
                        getActivity(),
                        query,
                        new String[]{BuddyfiedContract.AttributeEntry.COLUMN_NAME},
                        null,
                        null,
                        BuddyfiedContract.AttributeEntry.COLUMN_NAME + " ASC");
            }
        }
        return retVal;
    }

    private void setRowName(int row, String value) {
        if ((-1 != row) && (mData.length > row)) {
            mData[row].name = value;
        }
    }

    private void setRowValue(int row, String value) {
        if ((-1 != row) && (mData.length > row)) {
            mData[row].value = value;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        if (LoaderID.PROFILE_MAIN == loaderId) {
            // we can unpack AGE, COMMENTS, IMAGE
            if (data.moveToFirst()) {
                setRowValue(COMMENTS_ROW, data.getString(COL_COMMENTS));
                setRowValue(AGE_ROW, data.getString(COL_AGE));
                setRowName(HEADER_ROW, data.getString(COL_NAME));
                setRowValue(HEADER_ROW, data.getString(COL_IMAGE_URI));
            }
        } else {
            // find the correct ProfileRow to update based on the loaderId
            LoaderListItem row = null;
            for (int i = 0; i < mData.length; i++) {
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
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
