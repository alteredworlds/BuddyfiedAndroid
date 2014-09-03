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
import com.alteredworlds.buddyfied.Utils;
import com.alteredworlds.buddyfied.data.BuddyfiedContract;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.BuddyEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeListEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedProvider;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by twcgilbert on 21/08/2014.
 */
public class BuddyQueryService extends IntentService {
    private static final String LOG_TAG = BuddyQueryService.class.getSimpleName();

    public static final String METHOD_EXTRA = "method";
    public static final String ID_EXTRA = "id";
    public static final String RESULT_RECEIVER_EXTRA = "result_receiver";
    public static final String RESULT_DESCRIPTION = "result_description";

    public static final String GetMatches = "bp.getMatches";
    public static final String GetMatchesIfNeeded = "GetMatchesIfNeeded";
    public static final String SendMessage = "bp.sendMessage";
    public static final String GetMemberInfo = "bp.getMemberData";
    public static final String VerifyConnection = "bp.verifyConnection";

    // this last one is a bit naughty - easiest way of providing
    // asynchronous delete
    public static final String DeleteBuddies = "deleteBuddies";

    public static final String BuddyXmlRpcRoot = "index.php?bp_xmlrpc=true";

    private static final int FIELD_ID_NAME = 1;
    private static final int FIELD_ID_COMMENTS = 167;
    private static final int FIELD_ID_AGE = 8;
    private static final int FIELD_ID_COUNTRY = 6;
    private static final int FIELD_ID_GAMEPLAY = 5;
    private static final int FIELD_ID_LANGUAGE = 153;
    private static final int FIELD_ID_PLATFORM = 2;
    private static final int FIELD_ID_PLAYING = 4;
    private static final int FIELD_ID_SKILL = 7;
    private static final int FIELD_ID_TIME = 152;
    private static final int FIELD_ID_VOICE = 9;

    public BuddyQueryService() {
        super("BuddyQueryService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER_EXTRA);
        String method = intent.getStringExtra(METHOD_EXTRA);
        CallResult result = null;
        if (0 == GetMatchesIfNeeded.compareTo(method)) {
            result = getMatches(intent, true);
        } else if (0 == GetMatches.compareTo(method)) {
            result = getMatches(intent, false);
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
        if ((null != resultReceiver) && (null != result)) {
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
        int resultCode = 0;
        Bundle resultBundle = null;
        String userId = Settings.getUserId(this);

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("user_id", userId);

        String uri = Settings.getBuddySite(this) + BuddyXmlRpcRoot;
        XMLRPCClient client = new XMLRPCClient(uri);
        try {
            Object res = client.call(
                    GetMemberInfo,
                    Settings.getUsername(this),
                    Settings.getPassword(this),
                    data);
            String resultMessage = processMemberInfoResults(res, userId);
            if (!Utils.isNullOrEmpty(resultMessage)) {
                resultBundle = getResultBundleWithDescription(resultMessage);
            }
        } catch (XMLRPCException e) {
            e.printStackTrace();
            resultBundle = getResultBundleWithDescription(e.getLocalizedMessage());
            resultCode = -1;
        }
        return new CallResult(resultCode, resultBundle);
    }

    private CallResult sendMessage(Intent intent) {
        //parameters:@[user, password, @{@"recipients": recipients, @"subject" : subject, @"content" : body}]
        int resultCode = 0;
        Bundle resultBundle = null;

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("recipients", Settings.getUserId(this));
        data.put("subject", Settings.getUserId(this));
        data.put("content", Settings.getUserId(this));

        String uri = Settings.getBuddySite(this) + BuddyXmlRpcRoot;
        XMLRPCClient client = new XMLRPCClient(uri);
        try {
            String resultMessage = null;
            Object res = client.call(
                    SendMessage,
                    Settings.getUsername(this),
                    Settings.getPassword(this),
                    data);
            if (res instanceof HashMap) {
                Object message = ((HashMap) res).get("message");
                if (message instanceof String) {
                    resultMessage = (String) message;
                }
            }
            if (!Utils.isNullOrEmpty(resultMessage)) {
                resultBundle = getResultBundleWithDescription(resultMessage);
            }
        } catch (XMLRPCException e) {
            e.printStackTrace();
            resultBundle = getResultBundleWithDescription(e.getLocalizedMessage());
            resultCode = -1;
        }
        return new CallResult(resultCode, resultBundle);
    }

    private HashMap<String, Object> getSearchParameters(Intent intent) {
        HashMap<String, Object> retVal = new HashMap<String, Object>();
        int profileId = intent.getIntExtra(ID_EXTRA, -1);
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

    private Boolean haveBuddiesAlready() {
        Cursor cursor = getContentResolver().query(
                BuddyEntry.CONTENT_URI,
                new String[]{BuddyEntry._ID},
                null,
                null,
                null);
        Boolean retVal = cursor.getCount() > 0;
        cursor.close();
        return retVal;
    }

    private CallResult getMatches(Intent intent, Boolean onlyIfNeeded) {
        int resultCode = 0;
        Bundle resultBundle = null;
        if (onlyIfNeeded && haveBuddiesAlready()) {
            // we don't need to make the call
        } else {
            // we need to retrieve buddies from the server
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
                    if (!Utils.isNullOrEmpty(resultMessage)) {
                        resultBundle = getResultBundleWithDescription(resultMessage);
                    }
                } catch (XMLRPCException e) {
                    e.printStackTrace();
                    resultBundle = getResultBundleWithDescription(e.getLocalizedMessage());
                    resultCode = -1;
                }
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

    private String processMemberInfoResults(Object res, String userIdStr) {
        String retVal = null;
        if (res instanceof HashMap) {
            Object message = ((HashMap) res).get("message");
            if (message instanceof String) {
                retVal = (String) message;
            } else if (message instanceof HashMap) {
                // this should be member info...
                int userId = Integer.parseInt(userIdStr);
                // start building Profile record to insert
                HashMap data = (HashMap) message;
                ContentValues profileCv = new ContentValues();
                profileCv.put(ProfileEntry._ID, userId);
                profileCv.put(ProfileEntry.COLUMN_NAME, (String) data.get("display_name"));
                profileCv.put(ProfileEntry.COLUMN_IMAGE_URI, getBuddyImageUrl(data));
                //
                Vector<ContentValues> profileAttributeCv = new Vector<ContentValues>();
                Object[] profileGroups = (Object[]) data.get("profile_groups");
                String groupLabel;
                for (Object group : profileGroups) {
                    groupLabel = (String) ((HashMap) group).get("label");
                    if (0 == "Your games profile".compareTo(groupLabel)) {
                        Object[] fields = (Object[]) ((HashMap) group).get("fields");
                        for (Object field : fields) {
                            try {
                                int serverFieldId = Integer.parseInt((String) ((HashMap) field).get("id"));
                                String value = (String) ((HashMap) field).get("value");
                                if (serverFieldId == FIELD_ID_COMMENTS) {
                                    profileCv.put(ProfileEntry.COLUMN_COMMENTS, value);
                                } else {
                                    addProfileAttributeEntriesForServerField(
                                            profileAttributeCv, userId, serverFieldId, value);
                                }
                            } catch (NumberFormatException ex) {
                                // ignore this malformed field
                                Log.e(LOG_TAG, "Invalid member data field " + field.toString());
                            }
                        }
                    }
                }
                // FIRST: remove any existing Attributes for this Profile
                getContentResolver().delete(
                        ProfileAttributeEntry.CONTENT_URI,
                        ProfileAttributeEntry.COLUMN_PROFILE_ID + " = " + userId,
                        null);
                //
                // write this Profile record to the database (updated if already present)
                getContentResolver().insert(ProfileEntry.CONTENT_URI, profileCv);
                //
                // now write any associated Attributes
                if (profileAttributeCv.size() > 0) {
                    ContentValues[] param = new ContentValues[profileAttributeCv.size()];
                    profileAttributeCv.toArray(param);
                    getContentResolver().bulkInsert(ProfileAttributeEntry.CONTENT_URI, param);
                }
            }
        }
        return retVal;
    }

    private void addProfileAttributeEntriesForServerField(Vector<ContentValues> cvs,
                                                          int userId,
                                                          int serverFieldId,
                                                          String value) {
        switch (serverFieldId) {
            case FIELD_ID_COUNTRY:
            case FIELD_ID_GAMEPLAY:
            case FIELD_ID_LANGUAGE:
            case FIELD_ID_PLATFORM:
            case FIELD_ID_PLAYING:
            case FIELD_ID_SKILL:
            case FIELD_ID_TIME:
            case FIELD_ID_VOICE:
                break;
            default:
                return;
        }
        if (!Utils.isNullOrEmpty(value)) {
            // OK, so value is one or more (as comma-delimited) AttributeEntry Id
            for (String attributeIdStr : value.split(",")) {
                try {
                    int attributeId = Integer.parseInt(attributeIdStr);
                    ContentValues retVal = new ContentValues();
                    // at this point need to split comma delimited list of
                    retVal.put(ProfileAttributeEntry.COLUMN_PROFILE_ID, userId);
                    retVal.put(ProfileAttributeEntry.COLUMN_ATTRIBUTE_ID, attributeId);
                    cvs.add(retVal);
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Ignoring non-numeric attribute id " + attributeIdStr);
                }
            }
        }
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
