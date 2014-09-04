package com.alteredworlds.buddyfied.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.alteredworlds.buddyfied.Utils;

import java.util.List;

/**
 * Created by twcgilbert on 25/07/2014.
 */
public class BuddyfiedContract {
    public static final String CONTENT_AUTHORITY = "com.alteredworlds.buddyfied";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PROFILE = "profile";
    public static final String PATH_BUDDY = "buddy";
    public static final String PATH_ATTRIBUTE = "attribute";
    public static final String PATH_ATTRIBUTE_ALL = "all";
    public static final String PATH_PROFILE_ATTRIBUTE = "profile_attribute";
    public static final String PATH_PROFILE_ATTRIBUTE_LIST = "profile_attribute_list";

    /* Inner class that defines the table contents of the profile table */
    public static final class ProfileEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROFILE).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE;

        public static final String TABLE_NAME = "profile";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_COMMENTS = "comments";
        public static final String COLUMN_IMAGE_URI = "image_uri";
        public static final String COLUMN_AGE = "age";


        public static Uri buildProfileUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the location table */
    public static final class AttributeEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ATTRIBUTE).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_ATTRIBUTE;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_ATTRIBUTE;

        public static final String TypePlatform = "platform";
        public static final String TypeCountry = "country";
        public static final String TypeGameplay = "gameplay";
        public static final String TypePlaying = "playing";
        public static final String TypeSkill = "skill";
        public static final String TypeTime = "time";
        public static final String TypeLanguage = "language";
        public static final String TypeAgeRange = "age_range";
        public static final String TypeVoice = "voice";


        public static final String TABLE_NAME = "attribute";

        // country|language|playing|gameplay ++
        public static final String COLUMN_TYPE = "type";

        // display name for this attribute
        public static final String COLUMN_NAME = "name";

        public static Uri buildAttributeUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildAttributeType(String attributeType) {
            return CONTENT_URI.buildUpon().appendPath(attributeType).build();
        }

        public static Uri buildAttributeTypeForProfile(String attributeType, long profileId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(attributeType).appendPath(String.valueOf(profileId)).build();
        }

        public static Uri buildAttributeTypeForProfileAll(String attributeType, long profileId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(attributeType)
                    .appendPath(String.valueOf(profileId))
                    .appendPath(PATH_ATTRIBUTE_ALL)
                    .build();
        }

        public static long getProfileIdFromAttributeTypeForProfileAllUri(Uri uri) {
            long retVal = -1;
            final List<String> segments = uri.getPathSegments();
            final int size = segments.size();
            if (size > 1) {
                String profileId = segments.get(size - 2);
                if (!Utils.isNullOrEmpty(profileId)) {
                    retVal = Long.parseLong(profileId);
                }
            }
            return retVal;
        }

        public static String getAttributeTypeFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getProfileIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }
    }

    /* Inner class that defines the table contents of the buddy_attribute table */
    public static final class ProfileAttributeEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROFILE_ATTRIBUTE).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE_ATTRIBUTE;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE_ATTRIBUTE;

        public static final String TABLE_NAME = "profile_attribute";

        // foreign key into profile table
        public static final String COLUMN_PROFILE_ID = "profile_id";

        // one half of foreign key into attribute table
        public static final String COLUMN_ATTRIBUTE_ID = "attribute_id";

        public static Uri buildProfileAttributeUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the contents of the cursor returning search data */
    public static final class ProfileAttributeListEntry {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROFILE_ATTRIBUTE_LIST).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE_ATTRIBUTE_LIST;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE_ATTRIBUTE_LIST;

        // attribute type
        public static final String COLUMN_ATTRIBUTE_TYPE = "type";

        // comma delimited list of attribute._id
        public static final String COLUMN_ATTRIBUTE_IDS = "attribute_ids";

        public static Uri buildProfileAttributeListUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    /* Inner class that defines the table contents of the location table */
    public static final class BuddyEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BUDDY).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_BUDDY;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_BUDDY;

        public static final String TABLE_NAME = "buddy";

        public static final String COLUMN_DISPLAY_ORDER = "display_order";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_COMMENTS = "comments";
        public static final String COLUMN_IMAGE_URI = "image_uri";
        public static final String COLUMN_AGE = "age";

        // comma separated server ID lists for each type of attribute
        public static final String COLUMN_COUNTRY = "country";
        public static final String COLUMN_GAMEPLAY = "gameplay";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_PLATFORM = "platform";
        public static final String COLUMN_PLAYING = "playing";
        public static final String COLUMN_SKILL = "skill";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_VOICE = "voice";


        public static Uri buildBuddyUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
