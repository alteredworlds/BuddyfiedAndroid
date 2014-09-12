package com.alteredworlds.buddyfied.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.alteredworlds.buddyfied.Constants;
import com.alteredworlds.buddyfied.R;
import com.alteredworlds.buddyfied.Settings;
import com.alteredworlds.buddyfied.Utils;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * Created by twcgilbert on 18/08/2014.
 */
public class StaticDataService extends IntentService {
    private static final String LOG_TAG = StaticDataService.class.getSimpleName();

    public static final String STATIC_DATA_SERVICE_RESULT_EVENT = "static_data_service_result_event";

    public static final String GET_ALL_IF_NEEDED = "get_all_if_needed";
    public static final String UPDATE_ALL = "update_all";
    public static final String GET_IF_NEEDED = "get_if_needed";
    public static final String UPDATE = "update";

    public static final String PlatformKey = "platform";
    public static final String CountryKey = "country";
    public static final String GameplayKey = "gameplay";
    public static final String GameKey = "game";
    public static final String SkillKey = "skill";
    public static final String TimeKey = "times";
    public static final String LanguagesKey = "languages";

    static final String BuddifiedStaticDataUrl =
    "wp-content/themes/buddyfied/_inc/ajax/autocomplete.php?mode=";

    static final String AgeRangeJSON = "[{\"id\":\"900800\",\"name\":\"16-19\"},{\"id\":\"900801\",\"name\":\"20-25\"},{\"id\":\"900802\",\"name\":\"26-35\"},{\"id\":\"900803\",\"name\":\"36-44\"},{\"id\":\"900804\",\"name\":\"45+\"}]";
    public static final int VOICE_ID_YES = 900900;
    public static final int VOICE_ID_NO = 900901;
    public static final String VOICE_YES = "Yes";
    public static final String VOICE_NO = "No";
    static final String VoiceJSON = "[{\"id\":\"" + VOICE_ID_YES + "\",\"name\":\"" + VOICE_YES +
            "\"},{\"id\":\"" + VOICE_ID_NO + "\",\"name\":\"" + VOICE_NO + "\"}]";

    private final static String[] AttributeTypes = {
            AttributeEntry.TypePlatform,
            AttributeEntry.TypeCountry,
            AttributeEntry.TypeGameplay,
            AttributeEntry.TypePlaying,
            AttributeEntry.TypeLanguage,
            AttributeEntry.TypeSkill,
            AttributeEntry.TypeTime,
            AttributeEntry.TypeAgeRange,
            AttributeEntry.TypeVoice
    };

    public StaticDataService() {
        super("StaticDataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle result = null;
        String method = intent.getStringExtra(Constants.METHOD_EXTRA);
        String attributeType = intent.getStringExtra(Constants.ID_EXTRA);
        if (0 == GET_ALL_IF_NEEDED.compareTo(method)) {
            result = loadAllAttributesIfNeeded();
        } else if (0 == UPDATE_ALL.compareTo(method)) {
            result = loadAllAttributes();
        } else if (0 == GET_IF_NEEDED.compareTo(method)) {
            result = loadAttributeIfNeeded(attributeType);
        } else if (0 == UPDATE.compareTo(method)) {
            result = loadAttribute(attributeType);
        } else {
            String errorMessage = "Unknown StaticDataService method call: '" + method + "'";
            Log.e(LOG_TAG, errorMessage);
            result = resultBundle(-1, errorMessage);
        }
        reportResult(result);
    }

    private Bundle loadAllAttributesIfNeeded() {
        Bundle retVal = null;
        for (String attributeType : AttributeTypes) {
            retVal = loadAttributeIfNeeded(attributeType);
            if (Constants.RESULT_OK != getResultCode(retVal)) {
                break;
            }
        }
        return retVal;
    }

    private Bundle loadAllAttributes() {
        Bundle retVal = null;
        for (String attributeType : AttributeTypes) {
            retVal = loadAttribute(attributeType);
            if (Constants.RESULT_OK != getResultCode(retVal)) {
                break;
            }
        }
        return retVal;
    }

    private Bundle loadAttributeIfNeeded(String attributeType) {
        Bundle retVal = null;
        Uri query = AttributeEntry.buildAttributeType(attributeType);
        Cursor cursor = getContentResolver().query(
                query,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        boolean needToLoadStatic = 0 == cursor.getCount();
        cursor.close();
        if (needToLoadStatic) {
            retVal = loadAttribute(attributeType);
        }
        return retVal;
    }

    private Bundle loadAttribute(String type) {
        Bundle retVal = null;
        ContentValues[] cvArray = null;
        long numRows = 0l;
        try {
            if (0 == AttributeEntry.TypeAgeRange.compareTo(type)) {
                cvArray = createArrayOfAttributeValues(AgeRangeJSON, AttributeEntry.TypeAgeRange);
            } else if (0 == AttributeEntry.TypeVoice.compareTo(type)) {
                cvArray = createArrayOfAttributeValues(VoiceJSON, AttributeEntry.TypeVoice);
            } else {
                // static data request for Attribute type
                String url = Settings.getBuddySite(this) + BuddifiedStaticDataUrl + remoteKeyForEntityNamed(type) + "&q=";
                JSONArray staticData = getJson(url);
                // JSON must now be parsed and converted to series of records to be inserted into database
                cvArray = createArrayOfAttributeValues(staticData, type);
            }
            if (null != cvArray) {
                numRows = getContentResolver().bulkInsert(AttributeEntry.CONTENT_URI, cvArray);
                Log.i(LOG_TAG, "Inserted for for Uri:" + AttributeEntry.CONTENT_URI + " type " + type +
                        " num rows: " + numRows);
            }
        } catch (JSONException e) {
            retVal = resultBundle(Constants.RESULT_FAIL,
                    getString(R.string.static_load_failed) + e.getLocalizedMessage());
        } catch (IOException e) {
            retVal = resultBundle(Constants.RESULT_FAIL,
                    getString(R.string.static_load_failed) + e.getLocalizedMessage());
        }
        return retVal;
    }

    private Bundle resultBundle(int code, String description) {
        Bundle retVal = new Bundle();
        retVal.putInt(Constants.RESULT_CODE, code);
        if (!Utils.isNullOrEmpty(description)) {
            retVal.putString(Constants.RESULT_DESCRIPTION, description);
        }
        return retVal;
    }

    private int getResultCode(Bundle bundle) {
        int retVal = 0;
        if (null != bundle) {
            retVal = bundle.getInt(Constants.RESULT_CODE);
        }
        return retVal;
    }

    private void reportResult(Bundle result) {
        if (null != result) {
            Log.d(LOG_TAG, "Reporting method call result via localBroadcast: " + result.toString());
            Intent intent = new Intent(STATIC_DATA_SERVICE_RESULT_EVENT);
            intent.putExtra(Constants.RESULT_BUNDLE, result);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    static ContentValues[] createArrayOfAttributeValues(String jsonString, String attributeType) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonString);
        return createArrayOfAttributeValues(jsonArray, attributeType);
    }


    static ContentValues[] createArrayOfAttributeValues(JSONArray jsonArray, String attributeType) {
        Vector<ContentValues> contentValues = new Vector<ContentValues>();
        for (int idx = 0; idx < jsonArray.length(); idx++) {
            try {
                ContentValues cv = new ContentValues();
                JSONObject attribute = jsonArray.getJSONObject(idx);
                //Log.i(LOG_TAG, attribute.toString());
                cv.put(AttributeEntry._ID, attribute.getInt("id"));
                cv.put(AttributeEntry.COLUMN_TYPE, attributeType);
                cv.put(AttributeEntry.COLUMN_NAME, attribute.getString("name").trim());
                contentValues.add(cv);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i(LOG_TAG, "Ignoring malformed attribute of type " + attributeType + "at position " + idx);
            }
        }
        ContentValues[] retVal = new ContentValues[contentValues.size()];
        contentValues.toArray(retVal);
        return retVal;
    }

    static JSONArray getJson(String url) throws IOException, JSONException {
        InputStream is = null;
        String result = "";

        // HTTP
        HttpClient httpclient = new DefaultHttpClient(); // for port 80 requests!
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        is = entity.getContent();

        // Read response to string
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        is.close();
        result = sb.toString();

        // Convert string to object
        return new JSONArray(result);
    }

    static String remoteKeyForEntityNamed(String entityName) {
        String retVal = null;
        if (0 == AttributeEntry.TypePlatform.compareTo(entityName)) {
            retVal = PlatformKey;
        }
        else if (0 == AttributeEntry.TypeCountry.compareTo(entityName)) {
            retVal = CountryKey;
        }
        else if (0 == AttributeEntry.TypePlaying.compareTo(entityName)) {
            retVal = GameKey;
        }
        else if (0 == AttributeEntry.TypeLanguage.compareTo(entityName)) {
            retVal = LanguagesKey;
        }
        else if (0 == AttributeEntry.TypeGameplay.compareTo(entityName)) {
            retVal = GameplayKey;
        }
        else if (0 == AttributeEntry.TypeSkill.compareTo(entityName)) {
            retVal = SkillKey;
        }
        else if (0 == AttributeEntry.TypeTime.compareTo(entityName)) {
            retVal = TimeKey;
        }
        return retVal;
    }
}
