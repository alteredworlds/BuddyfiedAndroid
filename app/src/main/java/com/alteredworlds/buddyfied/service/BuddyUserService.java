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
import com.alteredworlds.buddyfied.Utils;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeListEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedProvider;
import com.alteredworlds.buddyfied.user_management.BuddyUserManagement;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by twcgilbert on 24/09/2014.
 */
public class BuddyUserService extends Service {

    private static final String LOG_TAG = BuddyUserService.class.getSimpleName();

    public static final String BUDDY_USER_SERVICE_RESULT_EVENT = "buddy_user_service_result";

    public static final String CANCEL = "cancel";
    public static final String REGISTER = "register";
    public static final String UPDATE = "update";

    public final int ID_CANCEL = 0;
    public final int ID_REGISTER = 1;
    public final int ID_UPDATE = 2;

    private HashMap<String, String> mFieldIdsFromName;

    private static final String[] ProfileQueryCols = {
            ProfileEntry.COLUMN_COMMENTS,
            ProfileEntry.COLUMN_AGE
    };
    private static final int COLUMN_COMMENTS_IDX = 0;
    private static final int COLUMN_AGE_IDX = 1;

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
            }
        }


        @Override
        public void handleMessage(Message msg) {
            final int startID = msg.arg1;
            // only support one call at a time, kill any that might be running
            cancelRunningTask();
            if ((ID_CANCEL == msg.what) || (null == mClient)) {
                // if this is a cancel or a faulty config we're done
                stopSelf(startID);
            } else if (ID_UPDATE == msg.what) {
                Log.i(LOG_TAG, "Update user info not yet implemented");
                stopSelf(startID);
            } else if (ID_REGISTER == msg.what) {
                // let's see what the requested search might be...
                HashMap<String, Object> data = getProfileParams(msg.arg2);
                mClient.registerNewUser(
                        BuddyUserService.this,
                        Settings.getUsername(BuddyUserService.this),
                        Settings.getPassword(BuddyUserService.this),
                        Settings.getEmail(BuddyUserService.this),
                        data,
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Log.i(LOG_TAG, "registerUser result: " + response.toString());


                                stopSelf(startID);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                StringBuilder sb = new StringBuilder("Register user failed: ");
                                if (!TextUtils.isEmpty(responseString)) {
                                    sb.append(responseString);
                                }
                                if (null != throwable) {
                                    sb.append(" ");
                                    sb.append(throwable.toString());
                                }
                                Log.e(LOG_TAG, sb.toString());
                                stopSelf(startID);
                            }
                        });
            }
        }

        private void reportResult(int code, String description) {
            Bundle result = new Bundle();
            result.putInt(Constants.RESULT_CODE, code);
            if (!Utils.isNullOrEmpty(description)) {
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
        String methodName = intent.getStringExtra(Constants.METHOD_EXTRA);
        int method = ID_CANCEL;
        if (0 == REGISTER.compareTo(methodName)) {
            method = ID_REGISTER;
        } else if (0 == UPDATE.compareTo(methodName)) {
            method = ID_UPDATE;
        }
        Message msg = mServiceHandler.obtainMessage();
        msg.what = method;
        msg.arg1 = startId;
        msg.arg2 = (int) intent.getLongExtra(Constants.ID_EXTRA, -1);
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

    private HashMap<String, Object> getProfileParams(int profileId) {
        HashMap<String, Object> retVal = new HashMap<String, Object>();
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
                    retVal.put(type, value);
                }
                while (cursor.moveToNext());
            }
            cursor.close();
            // also need the information from the profile record itself
            query = ProfileEntry.buildProfileUri(profileId);
            cursor = getContentResolver().query(query, ProfileQueryCols, null, null, null);
            if (cursor.moveToFirst()) {
                String comments = cursor.getString(COLUMN_COMMENTS_IDX);
                if (!TextUtils.isEmpty(comments)) {
                    retVal.put(getServerFieldIdFromPropertyName("comments"), comments);
                }
                String age = cursor.getString(COLUMN_AGE_IDX);
                if (!TextUtils.isEmpty(age)) {
                    retVal.put(getServerFieldIdFromPropertyName("years"), age);
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
}
