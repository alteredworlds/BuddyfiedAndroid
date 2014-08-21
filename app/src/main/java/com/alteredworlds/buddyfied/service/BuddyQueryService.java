package com.alteredworlds.buddyfied.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.alteredworlds.buddyfied.Settings;
import com.alteredworlds.buddyfied.data.BuddyfiedContract;
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
        } else {
            String errorMessage = "Unknown xmlrpc method call: '" + method + "'";
            Log.e(LOG_TAG, errorMessage);
            Bundle res = new Bundle();
            res.putString(RESULT_DESCRIPTION, errorMessage);
            result = new CallResult(-1, res);
        }
        if (null != resultReceiver) {
            resultReceiver.send(result.code, result.results);
        }
    }

    private CallResult verifyConnection(Intent intent) {
        CallResult retVal = null;
        String username = Settings.getUsername(this);
        String password = Settings.getPassword(this);
        String uri = Settings.getBuddySite(this) + BuddyXmlRpcRoot;
        XMLRPCClient client = new XMLRPCClient(uri);
        try {
            client.call(VerifyConnection, username, password);
            retVal = new CallResult(0, null);
        } catch (XMLRPCException e) {
            e.printStackTrace();
            Bundle res = new Bundle();
            res.putString(RESULT_DESCRIPTION, e.getLocalizedMessage());
            retVal = new CallResult(-1, res);
        }
        return retVal;
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
                    if ((0 != BuddyfiedContract.AttributeEntry.TypeVoice.compareTo(type)) &&
                            (0 != BuddyfiedContract.AttributeEntry.TypeAgeRange.compareTo(type))) {
                        retVal.put(
                                type,
                                cursor.getString(BuddyfiedProvider.COL_ATTRIBUTE_LIST_IDS_INDEX));
                    }
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }
        return retVal;
    }

    private CallResult getMatches(Intent intent) {
        CallResult retVal = null;
        //
        // first we need the query data
        HashMap<String, Object> data = getSearchParameters(intent);
        if (!data.isEmpty()) {
            String uri = Settings.getBuddySite(this) + BuddyXmlRpcRoot;
            XMLRPCClient client = new XMLRPCClient(uri);
            try {
                // OK, go ahead and call...
                Object res = client.call(
                        GetMatches,
                        Settings.getUsername(this),
                        Settings.getPassword(this),
                        data);
                retVal = new CallResult(0, null);
            } catch (XMLRPCException e) {
                e.printStackTrace();
                Bundle res = new Bundle();
                res.putString(RESULT_DESCRIPTION, e.getLocalizedMessage());
                retVal = new CallResult(-1, res);
            }
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
