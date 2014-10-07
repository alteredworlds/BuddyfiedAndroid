/*
 * Copyright (c) 2014 Tom Gilbert <tom@alteredworlds.com> - All rights reserved.
 *
 * This file is part of Buddyfied Android.
 *
 * For applicable license please see LICENSE included with this distribution.
 */

package com.alteredworlds.buddyfied.user_management;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;

import java.util.HashMap;

public interface UserManagement {

    public void cancel(Context context);

    public void registerNewUser(final Context context,
                                final String user,
                                final String password,
                                final String email,
                                final HashMap<String, String> profileData,
                                final JsonHttpResponseHandler responseHandler);

    public void updateProfileForUser(final Context context,
                                     final String user,
                                     final String password,
                                     final HashMap<String, String> profileData,
                                     final JsonHttpResponseHandler responseHandler);
}
