package com.alteredworlds.buddyfied.test;

/**
 * Created by twcgilbert on 25/07/2014.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.Map;
import java.util.Set;

import com.alteredworlds.buddyfied.data.BuddyfiedContract;
import com.alteredworlds.buddyfied.data.BuddyfiedDbHelper;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.BuddyEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeEntry;


public class TestDb extends UtilsTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(BuddyfiedDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new BuddyfiedDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        BuddyfiedDbHelper dbHelper = new BuddyfiedDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        ContentValues contentValues = createProfileValues();
        long profileRowId = runTableInsertReadTest(db, ProfileEntry.TABLE_NAME, contentValues);

        contentValues = createAttributeValues();
        long attributeRowId = runTableInsertReadTest(db, AttributeEntry.TABLE_NAME, contentValues);

        contentValues = createBuddyValues();
        runTableInsertReadTest(db, BuddyEntry.TABLE_NAME, contentValues);

        contentValues = createProfileAttributeValues(profileRowId, attributeRowId);
        runTableInsertReadTest(db, ProfileAttributeEntry.TABLE_NAME, contentValues);

        dbHelper.close();
    }

    static long runTableInsertReadTest(SQLiteDatabase db, String table, ContentValues contentValues)
    {
        // TEST: WRITE to table
        long rowId = db.insert(table, null, contentValues);
        assertTrue(rowId != -1);
        //
        // TEST: READ from table
        Cursor cursor = db.query(
                table,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );
        validateCursor(cursor, contentValues);
        return rowId;
    }
}