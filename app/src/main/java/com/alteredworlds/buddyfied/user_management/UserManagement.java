package com.alteredworlds.buddyfied.user_management;

import android.content.Context;

import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.HashMap;

/**
 * Created by twcgilbert on 24/09/2014.
 */
public interface UserManagement {

    public void cancel(Context context);

    public void registerNewUser(Context context,
                                String user,
                                String password,
                                String email,
                                HashMap<String, Object> profileData,
                                AsyncHttpResponseHandler responseHandler);

    public void updateProfileForUser(Context context,
                                     String user,
                                     String password,
                                     HashMap<String, Object> profileData,
                                     AsyncHttpResponseHandler responseHandler);
}
