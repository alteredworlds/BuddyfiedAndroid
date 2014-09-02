package com.alteredworlds.buddyfied;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by twcgilbert on 30/07/2014.
 */
public class Settings {
    private final static String pref_username_key = "pref_username_key";
    private final static String pref_password_key = "pref_password_key";
    private final static String pref_userid_key = "pref_userid_key";
    private final static String pref_position_key = "pref_position_key";

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

    public static String getUserId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(pref_userid_key, null);
    }

    public static void setUserId(Context context, String username) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(pref_userid_key, username);
        edit.apply();
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
}
