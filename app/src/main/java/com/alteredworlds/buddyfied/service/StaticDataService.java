package com.alteredworlds.buddyfied.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.alteredworlds.buddyfied.Settings;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * Created by twcgilbert on 18/08/2014.
 */
public class StaticDataService extends IntentService {
    private static final String LOG_TAG = StaticDataService.class.getSimpleName();

    static public final String STATIC_QUERY_EXTRA = "type";

    public static final String PlatformKey = "platform";
    public static final String CountryKey = "country";
    public static final String GameplayKey = "gameplay";
    public static final String GameKey = "game";
    public static final String SkillKey = "skill";
    public static final String TimeKey = "times";
    public static final String LanguagesKey = "languages";

    static final String BuddifiedStaticDataUrl =
    "wp-content/themes/buddyfied/_inc/ajax/autocomplete.php?mode=";

    public StaticDataService() {
        super("StaticDataService");
    }

    public static void initialStaticDataLoadIfNeeded(ContextWrapper context) {
        // for each Attribute Type, see if we already have data
        // if not, kick off a request and load.
        loadStaticIfNecessaryForAttributeType(AttributeEntry.TypePlatform, context);
        loadStaticIfNecessaryForAttributeType(AttributeEntry.TypeCountry, context);
        loadStaticIfNecessaryForAttributeType(AttributeEntry.TypeGameplay, context);
        loadStaticIfNecessaryForAttributeType(AttributeEntry.TypePlaying, context);
        loadStaticIfNecessaryForAttributeType(AttributeEntry.TypeLanguage, context);
        loadStaticIfNecessaryForAttributeType(AttributeEntry.TypeSkill, context);
        loadStaticIfNecessaryForAttributeType(AttributeEntry.TypeTime, context);
    }

    public static void loadStaticIfNecessaryForAttributeType(String attributeType, ContextWrapper context) {
        Uri query = AttributeEntry.buildAttributeType(attributeType);
        Cursor cursor = context.getContentResolver().query(
                query,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        boolean needToLoadStatic = 0 == cursor.getCount();
        cursor.close();
        if (needToLoadStatic)
            loadStaticForAttributeType(attributeType, context);
    }

    public static void loadStaticForAttributeType(String attributeType, ContextWrapper context) {
        Intent intent = new Intent(context, StaticDataService.class);
        intent.putExtra(StaticDataService.STATIC_QUERY_EXTRA, attributeType);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String type = intent.getStringExtra(STATIC_QUERY_EXTRA);
        // static data request for Attribute type
        String url = Settings.getBuddySite(this) + BuddifiedStaticDataUrl + remoteKeyForEntityNamed(type) + "&q=";
        JSONArray staticData = getJson(url);
        // JSON must now be parsed and converted to series of records to be inserted into database
        ContentValues[] cvArray = createArrayOfAttributeValues(staticData, type);
        long numRows = getContentResolver().bulkInsert(AttributeEntry.CONTENT_URI, cvArray);
        Log.i(LOG_TAG, "Inserted for for Uri:" + AttributeEntry.CONTENT_URI +
                " num rows: " + numRows);
    }

    static ContentValues[] createArrayOfAttributeValues(JSONArray jsonArray, String attributeType) {
        Vector<ContentValues> contentValues = new Vector<ContentValues>();
        for (int idx = 0; idx < jsonArray.length(); idx++) {
            try {
                ContentValues cv = new ContentValues();
                JSONObject attribute = jsonArray.getJSONObject(idx);
                Log.i(LOG_TAG, attribute.toString());
                cv.put(AttributeEntry._ID, attribute.getInt("id"));
                cv.put(AttributeEntry.COLUMN_TYPE, attributeType);
                cv.put(AttributeEntry.COLUMN_NAME, attribute.getString("name"));
                contentValues.add(cv);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ContentValues[] retVal = new ContentValues[contentValues.size()];
        contentValues.toArray(retVal);
        return retVal;
    }

    static JSONArray getJson(String url){

        InputStream is = null;
        String result = "";
        JSONArray jsonArray = null;

        // HTTP
        try {
            HttpClient httpclient = new DefaultHttpClient(); // for port 80 requests!
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        // Read response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        // Convert string to object
        try {
            jsonArray = new JSONArray(result);
        } catch(JSONException e) {
            e.printStackTrace();
            return null;
        }

        return jsonArray;

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
