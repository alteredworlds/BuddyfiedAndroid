package com.alteredworlds.buddyfied.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.alteredworlds.buddyfied.data.BuddyfiedContract.BuddyEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeEntry;

/**
 * Created by twcgilbert on 27/07/2014.
 */
public class TestProvider extends UtilsTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    public void setUp()
    {
        deleteAllRecords();
    }

    public void testGetType()
    {
        // PROFILE
        String type = mContext.getContentResolver().getType(ProfileEntry.CONTENT_URI);
        assertEquals(ProfileEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(ProfileEntry.buildProfileUri(1L));
        assertEquals(ProfileEntry.CONTENT_ITEM_TYPE, type);
        //
        // ATTRIBUTE
        type = mContext.getContentResolver().getType(AttributeEntry.CONTENT_URI);
        assertEquals(AttributeEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(AttributeEntry.buildAttributeUri(1L));
        assertEquals(AttributeEntry.CONTENT_ITEM_TYPE, type);
        //
        // BUDDY
        type = mContext.getContentResolver().getType(BuddyEntry.CONTENT_URI);
        assertEquals(BuddyEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(BuddyEntry.buildBuddyUri(1L));
        assertEquals(BuddyEntry.CONTENT_ITEM_TYPE, type);
        //
        // PROFILE_ATTRIBUTE
        type = mContext.getContentResolver().getType(ProfileAttributeEntry.CONTENT_URI);
        assertEquals(ProfileAttributeEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(ProfileAttributeEntry.buildProfileAttributeUri(1L));
        assertEquals(ProfileAttributeEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testInsertReadProvider() {
        ContentValues contentValues = createProfileValues();
        long profileRowId = runInsertReadTest(ProfileEntry.CONTENT_URI, contentValues);

        contentValues = createAttributeValues();
        long attributeRowId = runInsertReadTest(AttributeEntry.CONTENT_URI, contentValues);

        contentValues = createBuddyValues();
        runInsertReadTest(BuddyEntry.CONTENT_URI, contentValues);

        contentValues = createProfileAttributeValues(profileRowId, attributeRowId);
        runInsertReadTest(ProfileAttributeEntry.CONTENT_URI, contentValues);
    }

    public long runInsertReadTest(Uri uri, ContentValues testValues)
    {
        Uri resultUri = mContext.getContentResolver().insert(uri, testValues);
        long retVal = ContentUris.parseId(resultUri);
        Log.i(LOG_TAG, "Inserted for for Uri:" + uri +
                " row with _ID " + retVal);
        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                uri,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        validateCursor(cursor, testValues);
        cursor.close();
        return retVal;
    }

    // brings our database to an empty state
    public void deleteAllRecords()
    {
        deleteAndTestAllRecordsForURI(ProfileAttributeEntry.CONTENT_URI);
        deleteAndTestAllRecordsForURI(AttributeEntry.CONTENT_URI);
        deleteAndTestAllRecordsForURI(ProfileEntry.CONTENT_URI);
        deleteAndTestAllRecordsForURI(BuddyEntry.CONTENT_URI);
    }

    public void deleteAndTestAllRecordsForURI(Uri uri)
    {
        mContext.getContentResolver().delete(
                uri,
                null,
                null
        );
        Cursor cursor = mContext.getContentResolver().query(
                uri,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }
}
