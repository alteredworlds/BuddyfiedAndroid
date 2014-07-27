package com.alteredworlds.buddyfied.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.alteredworlds.buddyfied.data.BuddyfiedContract.BuddyEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeEntry;

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

    @Override
    public boolean onCreate() {
        mOpenHelper = new BuddyfiedDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
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
        return null;
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
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
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
}
