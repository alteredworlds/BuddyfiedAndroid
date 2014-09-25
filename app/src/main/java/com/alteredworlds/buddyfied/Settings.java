package com.alteredworlds.buddyfied;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * Created by twcgilbert on 30/07/2014.
 */
public class Settings {
    private final static String pref_username_key = "pref_username_key";
    private final static String pref_password_key = "pref_password_key";
    private final static String pref_userid_key = "pref_userid_key";
    private final static String pref_position_key = "pref_position_key";
    private final static String pref_join_required_key = "pref_join_required";
    private final static String pref_email_key = "pref_email";

    public static String getBuddySite(Context context) {
        return context.getString(R.string.buddy_site);
    }

    public static String getUsername(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(pref_username_key, null);
    }

    public static void setUsername(Context context, String username) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(pref_username_key, username);
        edit.apply();
    }

    public static String getPassword(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(pref_password_key, null);
    }

    public static void setPassword(Context context, String username) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(pref_password_key, username);
        edit.apply();
    }

    public static Long getUserId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(pref_userid_key, -1);
    }

    public static void setUserId(Context context, Long userId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putLong(pref_userid_key, userId);
        edit.apply();
    }

    public static Boolean getJoinRequired(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(pref_join_required_key, false);
    }

    public static void setJoinRequired(Context context, Boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(pref_join_required_key, value);
        edit.apply();
    }

    public static Boolean isGuestUser(Context context) {
        String userName = getUsername(context);
        return !TextUtils.isEmpty(userName) && (0 == userName.compareTo(getGuestUsername(context)));
    }

    public static String getGuestUsername(Context context) {
        return context.getString(R.string.guest_username);
    }

    public static String getGuestPassword(Context context) {
        return context.getString(R.string.guest_password);
    }

    public static int getPosition(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(pref_position_key, 0);
    }

    public static void setPosition(Context context, int position) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(pref_position_key, position);
        edit.apply();
    }

    public static void clearPersonalSettings(Context context) {
        setPassword(context, "");
        setUsername(context, "");
        setUserId(context, -1l);
        setPosition(context, 0);
    }
}
