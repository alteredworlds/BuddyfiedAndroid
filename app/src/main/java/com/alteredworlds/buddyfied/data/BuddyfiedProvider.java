package com.alteredworlds.buddyfied.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.BuddyEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;

/**
 * Created by twcgilbert on 26/07/2014.
 */
public class BuddyfiedProvider extends ContentProvider {

    private static final int PROFILE = 100;
    private static final int PROFILE_ID = 101;
    private static final int ATTRIBUTE = 200;
    private static final int ATTRIBUTE_ID = 201;
    private static final int ATTRIBUTE_TYPE = 202;
    private static final int ATTRIBUTE_TYPE_FOR_PROFILE_ID = 203;
    private static final int BUDDY = 300;
    private static final int BUDDY_ID = 301;
    private static final int PROFILE_ATTRIBUTE = 400;
    private static final int PROFILE_ATTRIBUTE_ID = 401;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private BuddyfiedDbHelper mOpenHelper;

    private static final String sAttributeTypeSelection =
            AttributeEntry.TABLE_NAME + "." + AttributeEntry.COLUMN_TYPE + " = ? ";

    private static final String sAttributeTypeForProfileSelection =
            AttributeEntry.TABLE_NAME + "." + AttributeEntry.COLUMN_TYPE + " = ? AND " +
            ProfileAttributeEntry.TABLE_NAME + "." + ProfileAttributeEntry.COLUMN_PROFILE_ID + " = ? ";

    private static final SQLiteQueryBuilder sAttributeByTypeForProfileQueryBuilder;

    static {
        sAttributeByTypeForProfileQueryBuilder = new SQLiteQueryBuilder();
        sAttributeByTypeForProfileQueryBuilder.setTables(
                ProfileAttributeEntry.TABLE_NAME + " INNER JOIN " + AttributeEntry.TABLE_NAME +
                        " ON " +
                        ProfileAttributeEntry.TABLE_NAME + "." + ProfileAttributeEntry.COLUMN_ATTRIBUTE_ID +
                        " = " +
                        AttributeEntry.TABLE_NAME + "." + AttributeEntry._ID);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new BuddyfiedDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor = null;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            // "attribute/*/#"
            case ATTRIBUTE_TYPE_FOR_PROFILE_ID: {
                retCursor = getAttributeByTypeForProfile(uri, projection, sortOrder);
                break;
            }
            // "attribute/*"
            case ATTRIBUTE_TYPE: {
                retCursor = getAttributeByType(uri, projection, sortOrder);
                break;
            }
            // "attribute/#"
            case ATTRIBUTE_ID: {
                // extract ID and add a where clause
                long id = ContentUris.parseId(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        AttributeEntry.TABLE_NAME,
                        projection,
                        AttributeEntry._ID + " = " + id,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "attribute"
            case ATTRIBUTE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        AttributeEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            //
            // "profile/#"
            case PROFILE_ID: {
                // extract ID and add a where clause
                long id = ContentUris.parseId(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ProfileEntry.TABLE_NAME,
                        projection,
                        ProfileEntry._ID + " = " + id,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "profile"
            case PROFILE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ProfileEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            //
            // "buddy/#"
            case BUDDY_ID: {
                // extract ID and add a where clause
                long id = ContentUris.parseId(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        BuddyEntry.TABLE_NAME,
                        projection,
                        BuddyEntry._ID + " = " + id,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "buddy"
            case BUDDY: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        BuddyEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "profile_attribute/#"
            case PROFILE_ATTRIBUTE_ID: {
                // extract ID and add a where clause
                long id = ContentUris.parseId(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ProfileAttributeEntry.TABLE_NAME,
                        projection,
                        ProfileAttributeEntry._ID + " = " + id,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "profile_attribute"
            case PROFILE_ATTRIBUTE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ProfileAttributeEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (null != retCursor)
        {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return retCursor;
    }

    private Cursor getAttributeByTypeForProfile(Uri uri, String[] projection, String sortOrder) {
        // extract AttributeEntry and add a where clause
        String attributeType = AttributeEntry.getAttributeTypeFromUri(uri);
        long profileId = AttributeEntry.getProfileIdFromUri(uri);
        return sAttributeByTypeForProfileQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sAttributeTypeForProfileSelection,
                new String[] {attributeType, String.valueOf(profileId)},
                null,
                null,
                sortOrder);
    }

    private Cursor getAttributeByType(Uri uri, String[] projection, String sortOrder) {
        Cursor retCursor = null;
        // extract AttributeEntry and add a where clause
        String attributeType = AttributeEntry.getAttributeTypeFromUri(uri);
        retCursor = mOpenHelper.getReadableDatabase().query(
                AttributeEntry.TABLE_NAME,
                projection,
                sAttributeTypeSelection,
                new String[] {attributeType},
                null,
                null,
                null,
                sortOrder
        );
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case ATTRIBUTE_TYPE_FOR_PROFILE_ID:
                return AttributeEntry.CONTENT_ITEM_TYPE;
            case ATTRIBUTE_ID:
                return AttributeEntry.CONTENT_ITEM_TYPE;
            case ATTRIBUTE_TYPE:
                return AttributeEntry.CONTENT_TYPE;
            case ATTRIBUTE:
                return AttributeEntry.CONTENT_TYPE;
            case PROFILE_ID:
                return ProfileEntry.CONTENT_ITEM_TYPE;
            case PROFILE:
                return ProfileEntry.CONTENT_TYPE;
            case BUDDY_ID:
                return BuddyEntry.CONTENT_ITEM_TYPE;
            case BUDDY:
                return BuddyEntry.CONTENT_TYPE;
            case PROFILE_ATTRIBUTE_ID:
                return ProfileAttributeEntry.CONTENT_ITEM_TYPE;
            case PROFILE_ATTRIBUTE:
                return ProfileAttributeEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri retVal = null;
        switch (match)
        {
            case ATTRIBUTE: {
                long _id = db.insert(AttributeEntry.TABLE_NAME, null, contentValues);
                if (_id >= 0)
                    retVal = AttributeEntry.buildAttributeUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + AttributeEntry.TABLE_NAME);
            }
            break;
            case PROFILE: {
                long _id = db.insert(ProfileEntry.TABLE_NAME, null, contentValues);
                if (_id >= 0)
                    retVal = ProfileEntry.buildProfileUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + ProfileEntry.TABLE_NAME);
            }
                break;
            case BUDDY: {
                long _id = db.insert(BuddyEntry.TABLE_NAME, null, contentValues);
                if (_id >= 0)
                    retVal = BuddyEntry.buildBuddyUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + BuddyEntry.TABLE_NAME);
            }
            break;
            case PROFILE_ATTRIBUTE: {
                long _id = db.insert(ProfileAttributeEntry.TABLE_NAME, null, contentValues);
                if (_id >= 0)
                    retVal = ProfileAttributeEntry.buildProfileAttributeUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + ProfileAttributeEntry.TABLE_NAME);
            }
            break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numRows = 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case ATTRIBUTE:
                numRows = db.delete(AttributeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PROFILE:
                numRows = db.delete(ProfileEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BUDDY:
                numRows = db.delete(BuddyEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PROFILE_ATTRIBUTE:
                numRows = db.delete(ProfileAttributeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if ((null == selection) || (0 != numRows))
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int numRows = 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case ATTRIBUTE:
                numRows = db.update(AttributeEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case PROFILE:
                numRows = db.update(ProfileEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case BUDDY:
                numRows = db.update(BuddyEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case PROFILE_ATTRIBUTE:
                numRows = db.update(ProfileAttributeEntry.TABLE_NAME, values, selection, selectionArgs);
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if ((null == selection) || (0 != numRows))
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRows;
    }


    private static UriMatcher buildUriMatcher()
    {
        final UriMatcher retVal = new UriMatcher(UriMatcher.NO_MATCH);
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_PROFILE,
                PROFILE);
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_PROFILE + "/#",
                PROFILE_ID);
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_ATTRIBUTE ,
                ATTRIBUTE);
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_ATTRIBUTE + "/#" ,
                ATTRIBUTE_ID);
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_ATTRIBUTE + "/*" ,
                ATTRIBUTE_TYPE);
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_ATTRIBUTE + "/*/#",
                ATTRIBUTE_TYPE_FOR_PROFILE_ID);
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_BUDDY ,
                BUDDY);
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_BUDDY + "/#" ,
                BUDDY_ID);
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_PROFILE_ATTRIBUTE ,
                PROFILE_ATTRIBUTE);
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_PROFILE_ATTRIBUTE + "/#" ,
                PROFILE_ATTRIBUTE_ID);
        return retVal;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int retVal = 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ATTRIBUTE:
                db.beginTransaction();
                try {
                    for (ContentValues attributeValues : values) {
                        long _id = db.insertWithOnConflict(AttributeEntry.TABLE_NAME, null, attributeValues, SQLiteDatabase.CONFLICT_REPLACE);
                        if (-1 != _id) {
                            retVal++;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case BUDDY:
                db.beginTransaction();
                try {
                    for (ContentValues attributeValues : values) {
                        long _id = db.insert(BuddyEntry.TABLE_NAME, null, attributeValues);
                        if (-1 != _id) {
                            retVal++;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            default:
                retVal = super.bulkInsert(uri, values);
                break;
        }
        return retVal;
    }
}