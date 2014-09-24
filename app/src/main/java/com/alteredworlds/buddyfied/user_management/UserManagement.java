package com.alteredworlds.buddyfied.user_management;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;

import java.util.HashMap;

/**
 * Created by twcgilbert on 24/09/2014.
 */
public interface UserManagement {

    public void cancel(Context context);

    public void registerNewUser(final Context context,
                                final String user,
                                final String password,
                                final String email,
                                final HashMap<String, Object> profileData,
                                final JsonHttpResponseHandler responseHandler);

    public void updateProfileForUser(final Context context,
                                     final String user,
                                     final String password,
                                     final HashMap<String, Object> profileData,
                                     final JsonHttpResponseHandler responseHandler);
}
