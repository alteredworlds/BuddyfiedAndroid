package com.alteredworlds.buddyfied.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.alteredworlds.buddyfied.Constants;
import com.alteredworlds.buddyfied.R;
import com.alteredworlds.buddyfied.Settings;
import com.alteredworlds.buddyfied.data.BuddyfiedContract;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.BuddyEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeListEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedDbHelper;
import com.alteredworlds.buddyfied.data.BuddyfiedProvider;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Vector;

import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;

/**
 * Created by twcgilbert on 21/08/2014.
 */
public class BuddyQueryService extends IntentService {
    private static final String LOG_TAG = BuddyQueryService.class.getSimpleName();

    public static final String SUBJECT_EXTRA = "subject";
    public static final String BODY_EXTRA = "content";

    public static final String BUDDY_QUERY_SERVICE_RESULT_EVENT = "buddy_query_service_result";

    public static final String GetMatches = "bp.getMatches";
    public static final String SendMessage = "bp.sendMessage";
    public static final String GetMemberInfo = "bp.getMemberData";
    public static final String VerifyConnection = "bp.verifyConnection";

    // these are just used to provide asynchronous database processing
    public static final String CreateSearchProfileIfNeeded = "CreateSearchProfileIfNeeded";
    public static final String ClearDataOnLogout = "ClearDataOnLogout";
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
        String method = intent.getStringExtra(Constants.METHOD_EXTRA);
        Bundle result = null;
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
        } else if (0 == CreateSearchProfileIfNeeded.compareTo(method)) {
            result = createSearchProfileIfNeeded(intent);
        } else if (0 == ClearDataOnLogout.compareTo(method)) {
            result = clearDataOnLogout(intent);
        } else {
            String errorMessage = "Unknown xmlrpc method call: '" + method + "'";
            Log.e(LOG_TAG, errorMessage);
            result = resultBundle(-1, errorMessage);
        }
        reportResult(result);
    }

    private Bundle clearDataOnLogout(Intent intent) {
        getContentResolver().delete(ProfileAttributeEntry.CONTENT_URI, null, null);
        getContentResolver().delete(ProfileEntry.CONTENT_URI, null, null);
        getContentResolver().delete(BuddyEntry.CONTENT_URI, null, null);
        return null;
    }

    private Bundle createSearchProfileIfNeeded(Intent intent) {
        Cursor cursor = getContentResolver().query(ProfileEntry.CONTENT_URI,
                new String[]{ProfileEntry._ID},
                ProfileEntry._ID + " = " + BuddyfiedDbHelper.SEARCH_PROFILE_ID,
                null, null);
        if ((null != cursor) && cursor.moveToFirst()) {
            // we have a search profile already
        } else {
            ContentValues row = new ContentValues();
            row.put(ProfileEntry._ID, BuddyfiedDbHelper.SEARCH_PROFILE_ID);
            row.put(ProfileEntry.COLUMN_NAME, BuddyfiedDbHelper.SEARCH_PROFILE_NAME);
            getContentResolver().insert(ProfileEntry.CONTENT_URI, row);
        }
        return null;
    }

    private void reportResult(Bundle result) {
        if (null != result) {
            Log.d(LOG_TAG, "Reporting method call result via localBroadcast: " + result.toString());
            Intent intent = new Intent(BUDDY_QUERY_SERVICE_RESULT_EVENT);
            intent.putExtra(Constants.RESULT_BUNDLE, result);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private Bundle deleteAllBuddies(Intent intent) {
        getContentResolver().delete(BuddyEntry.CONTENT_URI, null, null);
        return null; // no need to report a result here
    }

    private Bundle verifyConnection(Intent intent) {
        int resultCode = Constants.RESULT_OK;
        String resultDescription = null;
        String username = Settings.getUsername(this);
        String password = Settings.getPassword(this);
        String uri = Settings.getBuddySite(this) + BuddyXmlRpcRoot;
        try {
            XMLRPCClient client = new XMLRPCClient(uri);
            Object res = client.call(VerifyConnection, username, password);
            if (res instanceof HashMap) {
                // we get a blasted Integer here for some reason
                Long userId = (long) (int) (Integer) ((HashMap) res).get("user_id");
                Settings.setUserId(this, userId);
            }
        } catch (XMLRPCException e) {
            e.printStackTrace();
            resultDescription = e.getLocalizedMessage();
            resultCode = Constants.RESULT_FAIL;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return resultBundle(resultCode, resultDescription);
    }

    private Bundle getMemberInfo(Intent intent) {
        int resultCode = Constants.RESULT_OK;
        String resultDescription = null;
        Long userId = intent.getLongExtra(Constants.ID_EXTRA, -1);

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("user_id", userId);

        String uri = Settings.getBuddySite(this) + BuddyXmlRpcRoot;
        try {
            XMLRPCClient client = new XMLRPCClient(uri);
            Object res = client.call(
                    GetMemberInfo,
                    Settings.getUsername(this),
                    Settings.getPassword(this),
                    data);
            resultDescription = processMemberInfoResults(res, userId);
        } catch (XMLRPCException e) {
            e.printStackTrace();
            resultDescription = e.getLocalizedMessage();
            resultCode = Constants.RESULT_FAIL;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return resultBundle(resultCode, resultDescription);
    }

    private Bundle sendMessage(Intent intent) {
        //parameters:@[user, password, @{@"recipients": recipients, @"subject" : subject, @"content" : body}]
        int resultCode = Constants.RESULT_OK;
        String resultDescription = null;

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("recipients", intent.getStringExtra(Constants.ID_EXTRA));
        data.put("subject", intent.getStringExtra(SUBJECT_EXTRA));
        data.put("content", intent.getStringExtra(BODY_EXTRA));

        String uri = Settings.getBuddySite(this) + BuddyXmlRpcRoot;

        try {
            XMLRPCClient client = new XMLRPCClient(uri);
            Object res = client.call(
                    SendMessage,
                    Settings.getUsername(this),
                    Settings.getPassword(this),
                    data);
            if (res instanceof HashMap) {
                Object message = ((HashMap) res).get("message");
                if (message instanceof String) {
                    resultDescription = (String) message;
                }
            }
        } catch (XMLRPCException e) {
            e.printStackTrace();
            resultDescription = e.getLocalizedMessage();
            resultCode = Constants.RESULT_FAIL;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return resultBundle(resultCode, resultDescription);
    }

    private HashMap<String, Object> getSearchParameters(Intent intent) {
        HashMap<String, Object> retVal = new HashMap<String, Object>();
        int profileId = intent.getIntExtra(Constants.ID_EXTRA, -1);
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

    private Bundle getMatches(Intent intent) {
        int resultCode = Constants.RESULT_OK;
        String resultDescription = null;
        // we need to retrieve buddies from the server
        HashMap<String, Object> data = getSearchParameters(intent);
        if (data.isEmpty()) {
            resultDescription = getString(R.string.specify_query);
        } else {
            String uri = Settings.getBuddySite(this) + BuddyXmlRpcRoot;

            try {
                XMLRPCClient client = new XMLRPCClient(uri);
                Object res = client.call(
                        GetMatches,
                        Settings.getUsername(this),
                        Settings.getPassword(this),
                        data);
                resultDescription = processSearchResults(res);
            } catch (XMLRPCException e) {
                e.printStackTrace();
                resultDescription = e.getLocalizedMessage();
                resultCode = Constants.RESULT_FAIL;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return resultBundle(resultCode, resultDescription);
    }

    private Bundle resultBundle(int code, String description) {
        Bundle retVal = new Bundle();
        retVal.putInt(Constants.RESULT_CODE, code);
        if (!TextUtils.isEmpty(description)) {
            retVal.putString(Constants.RESULT_DESCRIPTION, description);
        }
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

    private String processMemberInfoResults(Object res, Long userId) {
        String retVal = null;
        if (res instanceof HashMap) {
            Object message = ((HashMap) res).get("message");
            if (message instanceof String) {
                retVal = (String) message;
            } else if (message instanceof HashMap) {
                // this should be member info...
                // start building Profile record to insert
                HashMap data = (HashMap) message;
                ContentValues profileCv = new ContentValues();
                profileCv.put(ProfileEntry._ID, userId);
                profileCv.put(ProfileEntry.COLUMN_NAME, (String) data.get("display_name"));
                profileCv.put(ProfileEntry.COLUMN_IMAGE_URI, getBuddyImageUrl(data));
                //
                Vector<ContentValues> profileAttributeCv = new Vector<ContentValues>();
                Object[] profileGroups = (Object[]) data.get("profile_groups");
                String groupLabel, value;
                Object valueObj;
                for (Object group : profileGroups) {
                    groupLabel = (String) ((HashMap) group).get("label");
                    if (0 == "Your games profile".compareTo(groupLabel)) {
                        Object[] fields = (Object[]) ((HashMap) group).get("fields");
                        for (Object field : fields) {
                            try {
                                int serverFieldId = Integer.parseInt((String) ((HashMap) field).get("id"));
                                valueObj = ((HashMap) field).get("value");
                                if (valueObj instanceof String) {
                                    value = (String) valueObj;
                                    if (FIELD_ID_COMMENTS == serverFieldId) {
                                        profileCv.put(ProfileEntry.COLUMN_COMMENTS, value);
                                    } else if (FIELD_ID_AGE == serverFieldId) {
                                        profileCv.put(ProfileEntry.COLUMN_AGE, Integer.parseInt(value));
                                    } else {
                                        if (FIELD_ID_VOICE == serverFieldId) {
                                            value = String.valueOf((0 == StaticDataService.VOICE_YES.compareToIgnoreCase(value)) ?
                                                    StaticDataService.VOICE_ID_YES : StaticDataService.VOICE_ID_NO);
                                        }
                                        addProfileAttributeEntriesForServerField(
                                                profileAttributeCv, userId, serverFieldId, value);
                                    }
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
                                                          long userId,
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
        if (!TextUtils.isEmpty(value)) {
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
}
