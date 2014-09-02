package com.alteredworlds.buddyfied.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.alteredworlds.buddyfied.R;
import com.alteredworlds.buddyfied.Settings;
import com.alteredworlds.buddyfied.data.BuddyfiedContract;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.BuddyEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeListEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedProvider;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import java.util.HashMap;

/**
 * Created by twcgilbert on 21/08/2014.
 */
public class BuddyQueryService extends IntentService {
    private static final String LOG_TAG = BuddyQueryService.class.getSimpleName();

    public static final String METHOD_EXTRA = "method";
    public static final String PROFILE_ID_EXTRA = "profile_id";
    public static final String RESULT_RECEIVER_EXTRA = "result_receiver";
    public static final String RESULT_DESCRIPTION = "result_description";

    public static final String GetMatches = "bp.getMatches";
    public static final String SendMessage = "bp.sendMessage";
    public static final String GetMemberInfo = "bp.getMemberData";
    public static final String VerifyConnection = "bp.verifyConnection";

    // this last one is a bit naughty - easiest way of providing
    // asynchronous delete
    public static final String DeleteBuddies = "deleteBuddies";

    public static final String BuddyXmlRpcRoot = "index.php?bp_xmlrpc=true";

    public BuddyQueryService() {
        super("BuddyQueryService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER_EXTRA);
        String method = intent.getStringExtra(METHOD_EXTRA);
        CallResult result = null;
        if (0 == GetMatches.compareTo(method)) {
            result = getMatches(intent);
        } else if (0 == SendMessage.compareTo(method)) {
            result = sendMessage(intent);
        } else if (0 == GetMemberInfo.compareTo(method)) {
            result = getMemberInfo(intent);
        } else if (0 == VerifyConnection.compareTo(method)) {
            result = verifyConnection(intent);
        } else if (0 == DeleteBuddies.compareTo(method)) {
            result = deleteAllBuddies(intent);
        } else {
            String errorMessage = "Unknown xmlrpc method call: '" + method + "'";
            Log.e(LOG_TAG, errorMessage);
            Bundle resultBundle = getResultBundleWithDescription(errorMessage);
            result = new CallResult(-1, resultBundle);
        }
        if (null != resultReceiver) {
            resultReceiver.send(result.code, result.results);
        }
    }

    private CallResult deleteAllBuddies(Intent intent) {
        getContentResolver().delete(BuddyEntry.CONTENT_URI, null, null);
        return new CallResult(0, null);
    }

    private CallResult verifyConnection(Intent intent) {
        int resultCode = 0;
        Bundle resultBundle = null;
        String username = Settings.getUsername(this);
        String password = Settings.getPassword(this);
        String uri = Settings.getBuddySite(this) + BuddyXmlRpcRoot;
        XMLRPCClient client = new XMLRPCClient(uri);
        try {
            client.call(VerifyConnection, username, password);
        } catch (XMLRPCException e) {
            e.printStackTrace();
            resultBundle = getResultBundleWithDescription(e.getLocalizedMessage());
            resultCode = -1;
        }
        return new CallResult(resultCode, resultBundle);
    }

    private CallResult getMemberInfo(Intent intent) {
        return null;
    }

    private CallResult sendMessage(Intent intent) {
        return null;
    }

    private HashMap<String, Object> getSearchParameters(Intent intent) {
        HashMap<String, Object> retVal = new HashMap<String, Object>();
        int profileId = intent.getIntExtra(PROFILE_ID_EXTRA, -1);
        if (-1 != profileId) {
            Uri query = ProfileAttributeListEntry.buildProfileAttributeListUri(profileId);
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

    private CallResult getMatches(Intent intent) {
        int resultCode = 0;
        Bundle resultBundle = null;
        //
        HashMap<String, Object> data = getSearchParameters(intent);
        if (data.isEmpty()) {
            resultBundle = getResultBundleWithDescription(getString(R.string.specify_query));
        } else {
            String uri = Settings.getBuddySite(this) + BuddyXmlRpcRoot;
            XMLRPCClient client = new XMLRPCClient(uri);
            try {
                Object res = client.call(
                        GetMatches,
                        Settings.getUsername(this),
                        Settings.getPassword(this),
                        data);
                String resultMessage = processSearchResults(res);
                if ((null != resultMessage) && (resultMessage.length() > 0)) {
                    resultBundle = getResultBundleWithDescription(resultMessage);
                }
            } catch (XMLRPCException e) {
                e.printStackTrace();
                resultBundle = getResultBundleWithDescription(e.getLocalizedMessage());
                resultCode = -1;
            }
        }
        return new CallResult(resultCode, resultBundle);
    }

    private Bundle getResultBundleWithDescription(String description) {
        Bundle retVal = new Bundle();
        retVal.putString(RESULT_DESCRIPTION, description);
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
                int numBuddiesInserted = getContentResolver().bulkInsert(BuddyEntry.CONTENT_URI, cva);
                Log.i(LOG_TAG, "Inserted " + numBuddiesInserted + " buddies");
            }
        }
        return retVal;
    }

    private static ContentValues createBuddyValues(HashMap buddy, int displayOrder) {
        ContentValues retVal = new ContentValues();
        retVal.put(BuddyEntry._ID, (String) buddy.get("user_id"));
        retVal.put(BuddyEntry.COLUMN_DISPLAY_ORDER, displayOrder);
        retVal.put(BuddyEntry.COLUMN_NAME, (String) buddy.get("field_1"));
        retVal.put(BuddyEntry.COLUMN_COMMENTS, (String) buddy.get("field_167"));
        retVal.put(BuddyEntry.COLUMN_IMAGE_URI, getBuddyImageUrl(buddy));
        retVal.put(BuddyEntry.COLUMN_AGE, (String) buddy.get("field_8"));
        retVal.put(BuddyEntry.COLUMN_COUNTRY, (String) buddy.get("field_6"));
        retVal.put(BuddyEntry.COLUMN_GAMEPLAY, (String) buddy.get("field_5"));
        retVal.put(BuddyEntry.COLUMN_LANGUAGE, (String) buddy.get("field_153"));
        retVal.put(BuddyEntry.COLUMN_PLATFORM, (String) buddy.get("field_2"));
        retVal.put(BuddyEntry.COLUMN_PLAYING, (String) buddy.get("field_4"));
        retVal.put(BuddyEntry.COLUMN_SKILL, (String) buddy.get("field_7"));
        retVal.put(BuddyEntry.COLUMN_TIME, (String) buddy.get("field_152"));
        retVal.put(BuddyEntry.COLUMN_VOICE, (String) buddy.get("field_9"));
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


    private static class CallResult {
        public int code;
        public Bundle results;

        public CallResult(int code, Bundle results) {
            this.code = code;
            this.results = results;
        }
    }
}
