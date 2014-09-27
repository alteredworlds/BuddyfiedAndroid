package com.alteredworlds.buddyfied.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.alteredworlds.buddyfied.data.BuddyfiedContract.AttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.BuddyEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileAttributeEntry;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;

import java.util.Map;
import java.util.Set;

/**
 * Created by twcgilbert on 27/07/2014.
 */
public class UtilsTestCase extends AndroidTestCase {
    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

    static ContentValues createBuddyValues() {
        ContentValues retVal = new ContentValues();
        retVal.put(BuddyEntry.COLUMN_NAME, "alteredworlds");
        retVal.put(BuddyEntry.COLUMN_COMMENTS, "Yeah well where to begin mate");
        retVal.put(BuddyEntry.COLUMN_IMAGE_URI, "http://www.alteredworlds.com");
        retVal.put(BuddyEntry.COLUMN_AGE, "45");
        retVal.put(BuddyEntry.COLUMN_COUNTRY, "United Kingdom");
        retVal.put(BuddyEntry.COLUMN_GAMEPLAY, "fasdf");
        retVal.put(BuddyEntry.COLUMN_LANGUAGE, "dddd");
        retVal.put(BuddyEntry.COLUMN_PLATFORM, "Mac");
        retVal.put(BuddyEntry.COLUMN_PLAYING, "Painkiller Revenge");
        retVal.put(BuddyEntry.COLUMN_SKILL, "Superman");
        retVal.put(BuddyEntry.COLUMN_TIME, "Often");
        retVal.put(BuddyEntry.COLUMN_VOICE, "Yes");
        return retVal;
    }

    static ContentValues createAttributeValues() {
        ContentValues retVal = new ContentValues();
        retVal.put(AttributeEntry._ID, "1563");
        retVal.put(AttributeEntry.COLUMN_TYPE, "platform");
        retVal.put(AttributeEntry.COLUMN_NAME, "Playstation 4");
        return retVal;
    }

    static ContentValues createProfileValues() {
        ContentValues retVal = new ContentValues();
        retVal.put(ProfileEntry._ID, 1);
        retVal.put(ProfileEntry.COLUMN_NAME, "alteredworlds");
        retVal.put(ProfileEntry.COLUMN_COMMENTS, "Yeah well where to begin mate, really!");
        retVal.put(ProfileEntry.COLUMN_IMAGE_URI, "http://www.alteredworlds.com");
        return retVal;
    }

    static ContentValues createProfileAttributeValues(long profileRowId, long attributeRowId) {
        ContentValues retVal = new ContentValues();
        retVal.put(ProfileAttributeEntry.COLUMN_PROFILE_ID, profileRowId);
        retVal.put(ProfileAttributeEntry.COLUMN_ATTRIBUTE_ID, attributeRowId);
        return retVal;
    }
}
