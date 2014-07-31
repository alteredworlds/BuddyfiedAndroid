package com.alteredworlds.buddyfied;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by twcgilbert on 30/07/2014.
 */
public class Settings {
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static String getPreferredUnits(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric));
    }

}
