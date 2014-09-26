package com.alteredworlds.buddyfied.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.alteredworlds.buddyfied.Constants;
import com.alteredworlds.buddyfied.Settings;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeListEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedProvider;
import com.alteredworlds.buddyfied.user_management.BuddyUserManagement;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by twcgilbert on 24/09/2014.
 */
public class BuddyUserService extends Service {

    private static final String LOG_TAG = BuddyUserService.class.getSimpleName();

    public static final String BUDDY_USER_SERVICE_RESULT_EVENT = "buddy_user_service_result";

    public static final String CANCEL = "cancel";
    public static final String REGISTER = "register";
    public static final String UPDATE = "update";

    private HashMap<String, String> mFieldIdsFromName;

    private static final String[] ProfileQueryCols = {
            ProfileEntry.COLUMN_NAME,
            ProfileEntry.COLUMN_COMMENTS,
            ProfileEntry.COLUMN_AGE
    };
    private static final int COLUMN_NAME_IDX = 0;
    private static final int COLUMN_COMMENTS_IDX = 1;
    private static final int COLUMN_AGE_IDX = 2;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;


    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        private final BuddyUserManagement mClient;

        public ServiceHandler(Looper looper) {
            super(looper);
            mClient = new BuddyUserManagement();
            buildMapAttributeTypeToServerField();
        }

        private void cancelRunningTask() {
            if (null != mClient) {
                mClient.cancel(BuddyUserService.this);
                Log.d(LOG_TAG, "BuddyUserService cancelled");
            }
        }

        @Override
        public void handleMessage(Message msg) {
            final int startID = msg.what;
            final Bundle data = msg.getData();
            final String method = data.getString(Constants.METHOD_EXTRA);
            // only support one call at a time, kill any that might be running
            cancelRunningTask();
            if ((0 == CANCEL.compareTo(method)) || (null == mClient)) {
                // if this is a cancel or a faulty config we're done
                stopSelf(startID);
            } else if (0 == (UPDATE.compareTo(method))) {
                updateUser(startID, data);
            } else if (0 == (REGISTER.compareTo(method))) {
                registerUser(startID, data);
            }
        }

        private void updateUser(final int startID, final Bundle data) {
            final long profileId = data.getLong(Constants.ID_EXTRA);
            final String password = data.getString(Constants.PASSWORD_EXTRA);
            ProfileInfo userProfileInfo = getProfileParams(Settings.getUserId(BuddyUserService.this));
            ProfileInfo editProfileInfo = getProfileParams(profileId);
            HashMap<String, String> diffs = findChanges(userProfileInfo, editProfileInfo);
            mClient.updateProfileForUser(BuddyUserService.this,
                    userProfileInfo.mName,
                    password,
                    diffs,
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.i(LOG_TAG, "updateUser result: " + response.toString());
                            try {
                                String status = response.getString("status");
                                if (0 == "ok".compareTo(status)) {
                                    reportResult(Constants.RESULT_OK, null);
                                } else {
                                    reportResult(Constants.RESULT_FAIL, response.getString("error"));
                                }
                            } catch (JSONException e) {
                                reportResult(Constants.RESULT_FAIL, e.getLocalizedMessage());
                            }
                            stopSelf(startID);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            reportFailure("updateUser", responseString, throwable);
                            stopSelf(startID);
                        }
                    });
        }

        private void registerUser(final int startID, final Bundle data) {
            final long profileId = data.getLong(Constants.ID_EXTRA);
            final String password = data.getString(Constants.PASSWORD_EXTRA);
            final String email = data.getString(Constants.EMAIL_EXTRA);
            ProfileInfo profileInfo = getProfileParams(profileId);
            mClient.registerNewUser(
                    BuddyUserService.this,
                    profileInfo.mName,
                    password,
                    email,
                    profileInfo.mParams,
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.i(LOG_TAG, "registerUser result: " + response.toString());
                            try {
                                String status = response.getString("status");
                                if (0 == "ok".compareTo(status)) {
                                    reportResult(Constants.RESULT_OK, null);
                                } else {
                                    reportResult(Constants.RESULT_FAIL, response.getString("error"));
                                }
                            } catch (JSONException e) {
                                reportResult(Constants.RESULT_FAIL, e.getLocalizedMessage());
                            }
                            stopSelf(startID);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            reportFailure("registerUser", responseString, throwable);
                            stopSelf(startID);
                        }
                    });
        }

        private void reportFailure(String method, String responseString, Throwable throwable) {
            StringBuilder sb = new StringBuilder(method + " failed: ");
            if (!TextUtils.isEmpty(responseString)) {
                sb.append(responseString);
            }
            if (null != throwable) {
                sb.append(" ");
                sb.append(throwable.getLocalizedMessage());
            }
            String result = sb.toString();
            reportResult(Constants.RESULT_FAIL, sb.toString());
        }

        private void reportResult(int code, String description) {
            Bundle result = new Bundle();
            result.putInt(Constants.RESULT_CODE, code);
            if (!TextUtils.isEmpty(description)) {
                result.putString(Constants.RESULT_DESCRIPTION, description);
            }
            Log.d(LOG_TAG, "Reporting method call result via localBroadcast: " + result.toString());
            Intent intent = new Intent(BUDDY_USER_SERVICE_RESULT_EVENT);
            intent.putExtra(Constants.RESULT_BUNDLE, result);
            LocalBroadcastManager.getInstance(BuddyUserService.this).sendBroadcast(intent);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        //
        Message msg = mServiceHandler.obtainMessage();
        //
        Bundle data = new Bundle();
        msg.what = startId;
        data.putString(Constants.METHOD_EXTRA, intent.getStringExtra(Constants.METHOD_EXTRA));
        data.putLong(Constants.ID_EXTRA, intent.getLongExtra(Constants.ID_EXTRA, -1));
        data.putString(Constants.PASSWORD_EXTRA, intent.getStringExtra(Constants.PASSWORD_EXTRA));
        data.putString(Constants.EMAIL_EXTRA, intent.getStringExtra(Constants.EMAIL_EXTRA));
        msg.setData(data);
        //
        mServiceHandler.sendMessage(msg);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void buildMapAttributeTypeToServerField() {
        mFieldIdsFromName = new HashMap<String, String>();
        mFieldIdsFromName.put("name", "field_1");
        mFieldIdsFromName.put("platform", "field_2");
        mFieldIdsFromName.put("playing", "field_4");
        mFieldIdsFromName.put("gameplay", "field_5");
        mFieldIdsFromName.put("country", "field_6");
        mFieldIdsFromName.put("skill", "field_7");
        mFieldIdsFromName.put("years", "field_8");
        mFieldIdsFromName.put("voice", "field_9");
        mFieldIdsFromName.put("time", "field_152");
        mFieldIdsFromName.put("language", "field_153");
        mFieldIdsFromName.put("comments", "field_167");
    }

    private String getServerFieldIdFromPropertyName(String name) {
        return mFieldIdsFromName.get(name);
    }

    private HashMap<String, String> findChanges(ProfileInfo userProfileInfo, ProfileInfo editProfileInfo) {
        HashMap<String, String> retVal = new HashMap<String, String>(editProfileInfo.mParams);
        // any field that was present in the userProfile but is no longer present in the editProfile
        // has been 'blanked out' and needs to be present in the change set with value ""
        for (Map.Entry<String, String> entry : userProfileInfo.mParams.entrySet()) {
            if (!editProfileInfo.mParams.containsKey(entry.getKey())) {
                retVal.put(entry.getKey(), "");
            }
        }
        return retVal;
    }

    private ProfileInfo getProfileParams(long profileId) {
        ProfileInfo retVal = new ProfileInfo();
        if (-1 != profileId) {
            // get the associated attributes
            Uri query = ProfileAttributeListEntry.buildProfileAttributeListUri(profileId);
            Cursor cursor = getContentResolver().query(query, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(BuddyfiedProvider.COL_ATTRIBUTE_LIST_TYPE_INDEX);
                    String value = cursor.getString(BuddyfiedProvider.COL_ATTRIBUTE_LIST_IDS_INDEX);
                    // 1 param needs special treatment...
                    if (0 == AttributeEntry.TypeVoice.compareTo(type)) {
                        // need to re-transform the single ID back to Name
                        value = getAttributeNameForId(value);
                    }
                    // must transform the local property name / attribute type to
                    // what the server expects...
                    type = getServerFieldIdFromPropertyName(type);
                    retVal.mParams.put(type, value);
                }
                while (cursor.moveToNext());
            }
            cursor.close();
            // also need the information from the profile record itself
            query = ProfileEntry.buildProfileUri(profileId);
            cursor = getContentResolver().query(query, ProfileQueryCols, null, null, null);
            if (cursor.moveToFirst()) {
                retVal.mName = cursor.getString(COLUMN_NAME_IDX);
                String value = cursor.getString(COLUMN_COMMENTS_IDX);
                if (!TextUtils.isEmpty(value)) {
                    retVal.mParams.put(getServerFieldIdFromPropertyName("comments"), value);
                }
                value = cursor.getString(COLUMN_AGE_IDX);
                if (!TextUtils.isEmpty(value)) {
                    retVal.mParams.put(getServerFieldIdFromPropertyName("years"), value);
                }
            }
            cursor.close();
        }
        return retVal;
    }

    private String getAttributeNameForId(String attributeId) {
        String retVal = attributeId;
        Uri query = AttributeEntry.buildAttributeUri(Long.parseLong(attributeId));
        Cursor cursor = getContentResolver().query(
                query,
                new String[]{AttributeEntry.COLUMN_NAME},
                null, null, null);
        if (cursor.moveToFirst()) {
            retVal = cursor.getString(0);
        }
        cursor.close();
        return retVal;
    }

    private class ProfileInfo {
        public String mName;
        public HashMap<String, String> mParams;

        public ProfileInfo() {
            this(null);
        }

        public ProfileInfo(String name) {
            this(name, new HashMap<String, String>());
        }

        public ProfileInfo(String name, HashMap<String, String> params) {
            mName = name;
            mParams = params;
        }
    }
}
