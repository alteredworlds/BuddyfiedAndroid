package com.alteredworlds.buddyfied.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.BuddyEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;

/**
 * Created by twcgilbert on 25/07/2014.
 */
public class BuddyfiedDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = BuddyfiedDbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "buddyfied.db";

    public static final int SEARCH_PROFILE_ID = 1;
    public static final String SEARCH_PROFILE_NAME = "Default";

    public BuddyfiedDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // profile table
        final String SQL_CREATE_PROFILE_TABLE = "CREATE TABLE " + ProfileEntry.TABLE_NAME + " (" +
                ProfileEntry._ID + " INTEGER NOT NULL PRIMARY KEY ON CONFLICT REPLACE," +
                ProfileEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ProfileEntry.COLUMN_COMMENTS + " TEXT, " +
                ProfileEntry.COLUMN_IMAGE_URI + " TEXT, " +
                ProfileEntry.COLUMN_AGE + " INTEGER );";

        // attribute table
        final String SQL_CREATE_ATTRIBUTE_TABLE = "CREATE TABLE " + AttributeEntry.TABLE_NAME + " (" +
                AttributeEntry._ID + " INTEGER PRIMARY KEY," +
                AttributeEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                AttributeEntry.COLUMN_NAME + " TEXT NOT NULL );";

        // profile_attribute (link) table
        final String SQL_CREATE_PROFILE_ATTRIBUTE_TABLE = "CREATE TABLE " + BuddyfiedContract.ProfileAttributeEntry.TABLE_NAME + " (" +
                ProfileAttributeEntry._ID + " INTEGER PRIMARY KEY," +
                ProfileAttributeEntry.COLUMN_PROFILE_ID + " INTEGER NOT NULL, " +
                ProfileAttributeEntry.COLUMN_ATTRIBUTE_ID + " INTEGER NOT NULL, " +

                // Set up the profile_id column as a foreign key to profile table.
                " FOREIGN KEY (" + ProfileAttributeEntry.COLUMN_PROFILE_ID + ") REFERENCES " +
                ProfileEntry.TABLE_NAME + " (" + BuddyEntry._ID + "), " +

                // Set up the attribute_id column as a foreign key to attribute table.
                " FOREIGN KEY (" + ProfileAttributeEntry.COLUMN_ATTRIBUTE_ID + ") REFERENCES " +
                AttributeEntry.TABLE_NAME + " (" + BuddyEntry._ID + "), " +

                // not really sure if this is necessary but don't want multiple
                // repetitions of the same relationship
                // e.g.: profile '1' linked with attribute '27'
                " UNIQUE (" + ProfileAttributeEntry.COLUMN_PROFILE_ID + ", "
                + ProfileAttributeEntry.COLUMN_ATTRIBUTE_ID +
                ") ON CONFLICT IGNORE);";


        // buddy table
        final String SQL_CREATE_BUDDY_TABLE = "CREATE TABLE " + BuddyEntry.TABLE_NAME + " (" +
                BuddyEntry._ID + " INTEGER NOT NULL PRIMARY KEY ON CONFLICT REPLACE," +
                BuddyEntry.COLUMN_DISPLAY_ORDER + " INTEGER NOT NULL, " +
                BuddyEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                BuddyEntry.COLUMN_COMMENTS + " TEXT NOT NULL, " +
                BuddyEntry.COLUMN_IMAGE_URI + " TEXT NOT NULL, " +
                BuddyEntry.COLUMN_AGE + " TEXT NOT NULL," +
                BuddyEntry.COLUMN_COUNTRY + " TEXT NOT NULL, " +
                BuddyEntry.COLUMN_GAMEPLAY + " TEXT NOT NULL, " +
                BuddyEntry.COLUMN_LANGUAGE + " TEXT NOT NULL, " +
                BuddyEntry.COLUMN_PLATFORM + " TEXT NOT NULL, " +
                BuddyEntry.COLUMN_PLAYING + " TEXT NOT NULL, " +
                BuddyEntry.COLUMN_SKILL + " TEXT NOT NULL, " +
                BuddyEntry.COLUMN_TIME + " TEXT NOT NULL, " +
                BuddyEntry.COLUMN_VOICE + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_BUDDY_TABLE);
        db.execSQL(SQL_CREATE_PROFILE_TABLE);
        db.execSQL(SQL_CREATE_ATTRIBUTE_TABLE);
        db.execSQL(SQL_CREATE_PROFILE_ATTRIBUTE_TABLE);
        //
        //insertSearchProfileIfNeeded(db);
    }

//    public void insertSearchProfileIfNeeded(SQLiteDatabase db) {
//        Cursor cursor = db.query(ProfileEntry.TABLE_NAME,
//                new String[]{ProfileEntry._ID},
//                ProfileEntry._ID + " = " + SEARCH_PROFILE_ID,
//                null, null, null, null);
//        if ((null != cursor) && cursor.moveToFirst()) {
//            // we have a search profile already
//        } else {
//            insertProfile(db, SEARCH_PROFILE_ID, SEARCH_PROFILE_NAME);
//        }
//    }
//
//    protected void insertProfile(SQLiteDatabase db, int id, String name) {
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(ProfileEntry._ID, id);
//        contentValues.put(ProfileEntry.COLUMN_NAME, name);
//        long profileId = db.insert(ProfileEntry.TABLE_NAME, null, contentValues);
//        Log.i(LOG_TAG, "Profile '" + name + "' has ID " + profileId);
//    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BuddyEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProfileAttributeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AttributeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProfileEntry.TABLE_NAME);
        onCreate(db);
    }
}
