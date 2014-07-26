package com.alteredworlds.buddyfied.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
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
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
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
        return retVal;
    }
}
