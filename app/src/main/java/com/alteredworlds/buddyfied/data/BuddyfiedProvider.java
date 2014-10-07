/*
 * Copyright (c) 2014 Tom Gilbert <tom@alteredworlds.com> - All rights reserved.
 *
 * This file is part of Buddyfied Android.
 *
 * For applicable license please see LICENSE included with this distribution.
 */

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
import android.text.TextUtils;

import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.BuddyEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeListEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;

public class BuddyfiedProvider extends ContentProvider {

    private static final int PROFILE = 100;
    private static final int PROFILE_ID = 101;
    private static final int ATTRIBUTE = 200;
    private static final int ATTRIBUTE_ID = 201;
    private static final int ATTRIBUTE_TYPE = 202;
    private static final int ATTRIBUTE_TYPE_FOR_PROFILE_ID = 203;
    private static final int ATTRIBUTE_TYPE_FOR_PROFILE_ID_ALL = 204;
    private static final int BUDDY = 300;
    private static final int BUDDY_ID = 301;
    private static final int PROFILE_ATTRIBUTE = 400;
    private static final int PROFILE_ATTRIBUTE_ID = 401;
    private static final int PROFILE_ATTRIBUTE_LIST = 402;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private BuddyfiedDbHelper mOpenHelper;

    private static final String sAttributeTypeSelection =
            AttributeEntry.TABLE_NAME + "." + AttributeEntry.COLUMN_TYPE + " = ? ";

    private static final String sAttributeTypeForProfileSelection =
            AttributeEntry.TABLE_NAME + "." + AttributeEntry.COLUMN_TYPE + " = ? AND " +
                    ProfileAttributeEntry.TABLE_NAME + "." + ProfileAttributeEntry.COLUMN_PROFILE_ID + " = ? ";

    private static final String sAllAttributesByTypeForProfileSelectionP1 =
            "SELECT attribute._id, attribute.name, CASE WHEN EXISTS (SELECT * FROM profile_attribute WHERE profile_id = ";
    private static final String sAllAttributesByTypeForProfileSelectionP2 =
            " AND attribute_id = attribute._id) THEN 1 ELSE 0 END AS 'in_profile' FROM attribute WHERE attribute.type = ";

    private static final String sAttributeListsForProfileSelectP1 =
            "SELECT " + AttributeEntry.COLUMN_TYPE + ", GROUP_CONCAT(" +
                    AttributeEntry.TABLE_NAME + "." + AttributeEntry._ID + ") AS ids FROM " +
                    ProfileAttributeEntry.TABLE_NAME + " INNER JOIN " +
                    AttributeEntry.TABLE_NAME + " ON " +
                    ProfileAttributeEntry.COLUMN_ATTRIBUTE_ID + " = " +
                    AttributeEntry.TABLE_NAME + "." + AttributeEntry._ID +
                    " WHERE " + ProfileAttributeEntry.COLUMN_PROFILE_ID + " = ";
    private static final String sAttributeListsForProfileSelectP2 =
            " GROUP BY " + AttributeEntry.TABLE_NAME + "." + AttributeEntry.COLUMN_TYPE;

    public static final int COL_ATTRIBUTE_LIST_TYPE_INDEX = 0;
    public static final int COL_ATTRIBUTE_LIST_IDS_INDEX = 1;

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
            case ATTRIBUTE_TYPE_FOR_PROFILE_ID_ALL: {
                retCursor = getAllAttributesByTypeForProfile(uri, projection, selection, sortOrder);
                break;
            }
            // "attribute/*/#"
            case ATTRIBUTE_TYPE_FOR_PROFILE_ID: {
                retCursor = getAttributeByTypeForProfile(uri, projection, selection, sortOrder);
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

            case PROFILE_ATTRIBUTE_LIST: {
                retCursor = getAttributeListsForProfile(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (null != retCursor) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return retCursor;
    }

    private Cursor getAttributeListsForProfile(Uri uri, String[] projection, String sortOrder) {
        long profileId = AttributeEntry.getProfileIdFromUri(uri);
        final String queryString =
                sAttributeListsForProfileSelectP1 +
                        profileId +
                        sAttributeListsForProfileSelectP2;
        return mOpenHelper.getReadableDatabase().rawQuery(queryString, null);
    }

    private Cursor getAttributeByTypeForProfile(Uri uri, String[] projection, String selection, String sortOrder) {
        // extract AttributeEntry and add a where clause
        String attributeType = AttributeEntry.getAttributeTypeFromUri(uri);
        long profileId = AttributeEntry.getProfileIdFromUri(uri);
        StringBuilder sb = new StringBuilder(sAttributeTypeForProfileSelection);
        if (!TextUtils.isEmpty(selection)) {
            sb.append(" AND ");
            sb.append(selection);
        }
        return sAttributeByTypeForProfileQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sb.toString(),
                new String[]{attributeType, String.valueOf(profileId)},
                null,
                null,
                sortOrder);
    }

    private Cursor getAllAttributesByTypeForProfile(Uri uri, String[] projection, String selection, String sortOrder) {
        String attributeType = AttributeEntry.getAttributeTypeFromUri(uri);
        long profileId = AttributeEntry.getProfileIdFromAttributeTypeForProfileAllUri(uri);
        StringBuilder sb = new StringBuilder(sAllAttributesByTypeForProfileSelectionP1);
        sb.append(profileId + sAllAttributesByTypeForProfileSelectionP2 + "'" + attributeType + "'");
        if (!TextUtils.isEmpty(selection)) {
            sb.append(" AND ");
            sb.append(selection);
        }
        if (!TextUtils.isEmpty(sortOrder)) {
            sb.append(" ORDER BY ");
            sb.append(sortOrder);
        }
        return mOpenHelper.getReadableDatabase().rawQuery(sb.toString(), null);
    }

    private Cursor getAttributeByType(Uri uri, String[] projection, String sortOrder) {
        Cursor retCursor = null;
        // extract AttributeEntry and add a where clause
        String attributeType = AttributeEntry.getAttributeTypeFromUri(uri);
        retCursor = mOpenHelper.getReadableDatabase().query(
                AttributeEntry.TABLE_NAME,
                projection,
                sAttributeTypeSelection,
                new String[]{attributeType},
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
        switch (match) {
            case ATTRIBUTE_TYPE_FOR_PROFILE_ID_ALL:
                return AttributeEntry.CONTENT_TYPE;
            case ATTRIBUTE_TYPE_FOR_PROFILE_ID:
                return AttributeEntry.CONTENT_TYPE;
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
            case PROFILE_ATTRIBUTE_LIST:
                return ProfileAttributeListEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri retVal = null;
        switch (match) {
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
        switch (match) {
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
        if ((null == selection) || (0 != numRows)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int numRows = 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
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
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if ((null == selection) || (0 != numRows)) {
            getContext().getContentResolver().notifyChange(uri, null);
            if (PROFILE_ATTRIBUTE == match) {
                // this is a link table, but due to URI hierarchy need to explicitly notify
                // at AttributeEntry level to update related UIs
                getContext().getContentResolver().notifyChange(AttributeEntry.CONTENT_URI, null);
            }
        }
        return numRows;
    }


    private static UriMatcher buildUriMatcher() {
        final UriMatcher retVal = new UriMatcher(UriMatcher.NO_MATCH);
        //
        // all Profiles
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_PROFILE,
                PROFILE);
        //
        // a specific Profile (by ID)
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_PROFILE + "/#",
                PROFILE_ID);
        //
        // all Attributes
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_ATTRIBUTE,
                ATTRIBUTE);
        //
        // a specific Attribute (by ID)
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_ATTRIBUTE + "/#",
                ATTRIBUTE_ID);
        //
        // all Attributes of a given type
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_ATTRIBUTE + "/*",
                ATTRIBUTE_TYPE);
        //
        // all Attributes of a given type for a specific Profile (by ID)
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_ATTRIBUTE + "/*/#",
                ATTRIBUTE_TYPE_FOR_PROFILE_ID);
        //
        // all Attributes of a given type with indication if belong to specific Profile (by ID)
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_ATTRIBUTE + "/*/#/" + BuddyfiedContract.PATH_ATTRIBUTE_ALL,
                ATTRIBUTE_TYPE_FOR_PROFILE_ID_ALL);
        //
        // all Buddies
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_BUDDY,
                BUDDY);
        //
        // a specific Buddy (by ID)
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_BUDDY + "/#",
                BUDDY_ID);
        //
        // all Profile Attributes
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_PROFILE_ATTRIBUTE,
                PROFILE_ATTRIBUTE);
        //
        // a specific ProfileAttribute (by ID)
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_PROFILE_ATTRIBUTE + "/#",
                PROFILE_ATTRIBUTE_ID);
        //
        //
        retVal.addURI(
                BuddyfiedContract.CONTENT_AUTHORITY,
                BuddyfiedContract.PATH_PROFILE_ATTRIBUTE_LIST + "/#",
                PROFILE_ATTRIBUTE_LIST);
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
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case PROFILE_ATTRIBUTE:
                db.beginTransaction();
                try {
                    for (ContentValues attributeValues : values) {
                        long _id = db.insertWithOnConflict(ProfileAttributeEntry.TABLE_NAME, null, attributeValues, SQLiteDatabase.CONFLICT_REPLACE);
                        if (-1 != _id) {
                            retVal++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                getContext().getContentResolver().notifyChange(AttributeEntry.CONTENT_URI, null);
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
                } finally {
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