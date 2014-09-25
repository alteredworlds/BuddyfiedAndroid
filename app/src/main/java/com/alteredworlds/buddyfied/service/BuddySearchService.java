package com.alteredworlds.buddyfied.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.alteredworlds.buddyfied.Constants;
import com.alteredworlds.buddyfied.R;
import com.alteredworlds.buddyfied.Settings;
import com.alteredworlds.buddyfied.data.BuddyfiedContract;
import com.alteredworlds.buddyfied.data.BuddyfiedProvider;

import java.net.MalformedURLException;
import java.util.HashMap;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

/**
 * Created by twcgilbert on 08/09/2014.
 */
public class BuddySearchService extends Service {
    private static final String LOG_TAG = BuddySearchService.class.getSimpleName();

    public static final String GetMatches = "bp.getMatches";
    public static final String Cancel = "cancel";

    public static final String BUDDY_SEARCH_SERVICE_RESULT_EVENT = "buddy_search_service_result";

    public static final String BuddyXmlRpcRoot = "index.php?bp_xmlrpc=true";

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        private static final int NO_TASK = -1;
        private static final int NO_SEARCH = -1;
        private volatile long mRunningTaskId = NO_TASK;
        private volatile int mRunningTaskSearchIdentifier = NO_SEARCH;
        private XMLRPCClient mClient;

        public ServiceHandler(Looper looper) {
            super(looper);
            String uri = Settings.getBuddySite(BuddySearchService.this) + BuddyXmlRpcRoot;
            try {
                mClient = new XMLRPCClient(uri);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Failed to create BuddySearchService because " + e.getLocalizedMessage());
            }
        }

        private void cancelRunningTask() {
            if (NO_TASK != mRunningTaskId) {
                if (null != mClient) {
                    mClient.cancel(mRunningTaskId);
                }
                clearRunningTaskData();
            }
        }

        private void clearRunningTaskData() {
            mRunningTaskId = NO_TASK;
            mRunningTaskSearchIdentifier = NO_SEARCH;
        }

        @Override
        public void handleMessage(Message msg) {
            final int startID = msg.arg1;
            if ((CANCEL_ALL == msg.what) || (null == mClient)) {
                // if this is a cancel or a faulty config we're done
                cancelRunningTask();
                Log.d(LOG_TAG, "Cancelling Search");
                stopSelf(startID);
            } else {
                // let's see what the requested search might be...
                HashMap<String, Object> data = getSearchParameters(msg.arg2);
                int currentSearchIdentifier = data.hashCode();
                if (currentSearchIdentifier == mRunningTaskSearchIdentifier) {
                    // we are already running this search, no point repeating it
                    // don't report a result, just stop this particular invocation of
                    // the service since surplus to requirements
                    Log.d(LOG_TAG, "Search already running, no need to re-issue");
                    stopSelf(startID);
                } else {
                    // this is a new search
                    // only support one search at a time, kill any that might be running
                    cancelRunningTask();
                    if (data.isEmpty()) {
                        String resultDescription = getString(R.string.specify_query);
                        // not gonna search, report why...
                        reportResult(Constants.RESULT_OK, resultDescription);
                        // Stop the service using the startId
                        stopSelf(startID);
                    } else {
                        // we need to retrieve buddies from the server
                        // ok, time to make the call
                        mRunningTaskSearchIdentifier = currentSearchIdentifier;
                        mRunningTaskId = mClient.callAsync(
                                new XMLRPCCallback() {
                                    @Override
                                    public void onResponse(long id, Object result) {
                                        String resultDescription = processSearchResults(result);
                                        reportResult(Constants.RESULT_OK, resultDescription);
                                        clearRunningTaskData();
                                        stopSelf(startID);
                                    }

                                    @Override
                                    public void onError(long id, XMLRPCException error) {
                                        error.printStackTrace();
                                        reportResult(Constants.RESULT_FAIL, error.getLocalizedMessage());
                                        clearRunningTaskData();
                                        stopSelf(startID);
                                    }

                                    @Override
                                    public void onServerError(long id, XMLRPCServerException error) {
                                        error.printStackTrace();
                                        reportResult(Constants.RESULT_FAIL, error.getLocalizedMessage());
                                        clearRunningTaskData();
                                        stopSelf(startID);
                                    }
                                },
                                GetMatches,
                                Settings.getUsername(BuddySearchService.this),
                                Settings.getPassword(BuddySearchService.this),
                                data);
                    }
                }
            }
        }


        private void reportResult(int code, String description) {
            Bundle result = new Bundle();
            result.putInt(Constants.RESULT_CODE, code);
            if (!TextUtils.isEmpty(description)) {
                result.putString(Constants.RESULT_DESCRIPTION, description);
            }
            Log.d(LOG_TAG, "Reporting method call result via localBroadcast: " + result.toString());
            Intent intent = new Intent(BUDDY_SEARCH_SERVICE_RESULT_EVENT);
            intent.putExtra(Constants.RESULT_BUNDLE, result);
            LocalBroadcastManager.getInstance(BuddySearchService.this).sendBroadcast(intent);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    public final int SEARCH = 0;
    public final int CANCEL_ALL = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        //
        int method = SEARCH;
        if (0 == Cancel.compareTo(intent.getStringExtra(Constants.METHOD_EXTRA))) {
            method = CANCEL_ALL;
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
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "BuddySearchService done");
    }

    private HashMap<String, Object> getSearchParameters(int profileId) {
        HashMap<String, Object> retVal = new HashMap<String, Object>();
        if (-1 != profileId) {
            Uri query = BuddyfiedContract.ProfileAttributeListEntry.buildProfileAttributeListUri(profileId);
            Cursor cursor = getContentResolver().query(query, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(BuddyfiedProvider.COL_ATTRIBUTE_LIST_TYPE_INDEX);
                    String value = cursor.getString(BuddyfiedProvider.COL_ATTRIBUTE_LIST_IDS_INDEX);
                    // 2 params need special treatment...
                    if (0 == BuddyfiedContract.AttributeEntry.TypeVoice.compareTo(type)) {
                        type = "mic";
                        // need to re-transform the single ID back to Name
                        value = getAttributeNameForId(value);
                    } else if (0 == BuddyfiedContract.AttributeEntry.TypeAgeRange.compareTo(type)) {
                        type = "age";
                        // need to re-transform the single ID back to Name
                        value = getAttributeNameForId(value);
                    }
                    retVal.put(type, value);
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }
        return retVal;
    }

    private String getAttributeNameForId(String attributeId) {
        String retVal = attributeId;
        Uri query = BuddyfiedContract.AttributeEntry.buildAttributeUri(Long.parseLong(attributeId));
        Cursor cursor = getContentResolver().query(
                query,
                new String[]{BuddyfiedContract.AttributeEntry.COLUMN_NAME},
                null, null, null);
        if (cursor.moveToFirst()) {
            retVal = cursor.getString(0);
        }
        cursor.close();
        return retVal;
    }

    private String processSearchResults(Object res) {
        String retVal = null;
        if (res instanceof HashMap) {
            Object message = ((HashMap) res).get("message");
            if (message instanceof String) {
                retVal = (String) message;
            } else if (message instanceof Object[]) {
                // these should be Buddies...
                Object[] values = (Object[]) message;
                ContentValues[] cva = new ContentValues[values.length];
                for (int i = 0; i < values.length; i++) {
                    cva[i] = createBuddyValues((HashMap) values[i], i);
                }
                int numBuddiesInserted = getContentResolver().bulkInsert(BuddyfiedContract.BuddyEntry.CONTENT_URI, cva);
                Log.i(LOG_TAG, "Inserted " + numBuddiesInserted + " buddies");
            }
        }
        return retVal;
    }

    private static ContentValues createBuddyValues(HashMap buddy, int displayOrder) {
        ContentValues retVal = new ContentValues();
        retVal.put(BuddyfiedContract.BuddyEntry._ID, (String) buddy.get("user_id"));
        retVal.put(BuddyfiedContract.BuddyEntry.COLUMN_DISPLAY_ORDER, displayOrder);
        retVal.put(BuddyfiedContract.BuddyEntry.COLUMN_NAME, (String) buddy.get("field_1"));
        retVal.put(BuddyfiedContract.BuddyEntry.COLUMN_COMMENTS, (String) buddy.get("field_167"));
        retVal.put(BuddyfiedContract.BuddyEntry.COLUMN_IMAGE_URI, getBuddyImageUrl(buddy));
        retVal.put(BuddyfiedContract.BuddyEntry.COLUMN_AGE, (String) buddy.get("field_8"));
        retVal.put(BuddyfiedContract.BuddyEntry.COLUMN_VOICE, (String) buddy.get("field_9"));

        retVal.put(BuddyfiedContract.BuddyEntry.COLUMN_COUNTRY, cleanIdList(buddy, "field_6"));
        retVal.put(BuddyfiedContract.BuddyEntry.COLUMN_GAMEPLAY, cleanIdList(buddy, "field_5"));
        retVal.put(BuddyfiedContract.BuddyEntry.COLUMN_LANGUAGE, cleanIdList(buddy, "field_153"));
        retVal.put(BuddyfiedContract.BuddyEntry.COLUMN_PLATFORM, cleanIdList(buddy, "field_2"));
        retVal.put(BuddyfiedContract.BuddyEntry.COLUMN_PLAYING, cleanIdList(buddy, "field_4"));
        retVal.put(BuddyfiedContract.BuddyEntry.COLUMN_SKILL, cleanIdList(buddy, "field_7"));
        retVal.put(BuddyfiedContract.BuddyEntry.COLUMN_TIME, cleanIdList(buddy, "field_152"));

        return retVal;
    }

    private static String cleanIdList(HashMap buddy, String key) {
        String retVal = null;
        String inStr = (String) buddy.get(key);
        if ((null != inStr) && inStr.matches("[0-9]+(,[0-9]+)*")) {
            retVal = inStr;
        }
        return retVal;
    }

    private static String getBuddyImageUrl(HashMap buddy) {
        String retVal = "";
        Object sub = buddy.get("avatar");
        if (sub instanceof HashMap) {
            retVal = (String) ((HashMap) sub).get("full");
        }
        return retVal;
    }
}
