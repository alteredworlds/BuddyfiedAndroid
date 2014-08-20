package com.alteredworlds.buddyfied.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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

    public BuddyfiedDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create profile first
        final String SQL_CREATE_PROFILE_TABLE = "CREATE TABLE " + ProfileEntry.TABLE_NAME + " (" +
                ProfileEntry._ID + " INTEGER PRIMARY KEY," +
                ProfileEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ProfileEntry.COLUMN_COMMENTS + " TEXT, " +
                ProfileEntry.COLUMN_IMAGE_URI + " TEXT );";

                // To assure the application have just one profile with a given name
                // create a UNIQUE constraint with IGNORE strategy
                //" UNIQUE (" + ProfileEntry.COLUMN_NAME + ") ON CONFLICT IGNORE);";

        // create profile first
        final String SQL_CREATE_ATTRIBUTE_TABLE = "CREATE TABLE " + AttributeEntry.TABLE_NAME + " (" +
                AttributeEntry._ID + " INTEGER PRIMARY KEY," +
                AttributeEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                AttributeEntry.COLUMN_NAME + " TEXT NOT NULL );" ;

        // create profile first
        final String SQL_CREATE_BUDDY_ATTRIBUTE_TABLE = "CREATE TABLE " + BuddyfiedContract.ProfileAttributeEntry.TABLE_NAME + " (" +
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


        // then create weather that depends on location
        final String SQL_CREATE_BUDDY_TABLE = "CREATE TABLE " + BuddyEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for search results
                // it's reasonable to assume the user will want information
                // in the order in which it was received (& hence inserted)
                BuddyEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
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
                BuddyEntry.COLUMN_VOICE + " TEXT NOT NULL, " +
                //
                // a Buddy should have a unique name
                " UNIQUE (" + BuddyEntry.COLUMN_NAME + ") ON CONFLICT IGNORE);";

        db.execSQL(SQL_CREATE_BUDDY_TABLE);
        db.execSQL(SQL_CREATE_PROFILE_TABLE);
        db.execSQL(SQL_CREATE_ATTRIBUTE_TABLE);
        db.execSQL(SQL_CREATE_BUDDY_ATTRIBUTE_TABLE);
        //
        insertSearchProfile(db);
    }

    protected void insertSearchProfile(SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProfileEntry._ID, 1);
        contentValues.put(ProfileEntry.COLUMN_NAME, "Default");
        long searchProfileId = db.insert(ProfileEntry.TABLE_NAME, null, contentValues);
        Log.i(LOG_TAG, "Search Profile (Default) has ID " + searchProfileId);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BuddyEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProfileAttributeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AttributeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProfileEntry.TABLE_NAME);
        onCreate(db);
    }
}
