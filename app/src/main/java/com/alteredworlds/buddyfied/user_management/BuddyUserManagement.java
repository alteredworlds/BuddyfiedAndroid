package com.alteredworlds.buddyfied.user_management;

import android.content.Context;
import android.util.Log;

import com.alteredworlds.buddyfied.Settings;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by twcgilbert on 24/09/2014.
 */
public class BuddyUserManagement implements UserManagement {

    private final static String LOG_TAG = BuddyUserManagement.class.getSimpleName();

    private final static String sMagicTokenUrl = "api/get_nonce/?controller=user&method=";

    private final AsyncHttpClient mClient = new AsyncHttpClient();

    @Override
    public void cancel(Context context) {
        mClient.cancelRequests(context, true);
    }

    @Override
    public void registerNewUser(
            Context context,
            String user,
            String password,
            String email,
            HashMap<String, Object> profileData,
            final AsyncHttpResponseHandler responseHandler) {
        grabNonceForUser(context,
                user,
                password,
                "register",
                new JsonHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                    }

                    // this is success, anything else failure
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                    }
                });
    }

    @Override
    public void updateProfileForUser(
            Context context,
            String user,
            String password,
            HashMap<String, Object> profileData,
            AsyncHttpResponseHandler responseHandler) {

    }


    private void grabNonceForUser(Context context,
                                  String user,
                                  String password,
                                  String method,
                                  final JsonHttpResponseHandler responseHandler) {
        // FIRST we need the magic token, retrieved via an initial call
        String urlStr = Settings.getBuddySite(context) + sMagicTokenUrl + method;
        mClient.get(urlStr, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i(LOG_TAG, "nonce result: " + response.toString());
                responseHandler.onSuccess(statusCode, headers, response);
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                unexpectedResponse(statusCode, headers, throwable, errorResponse);
            }

            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                unexpectedResponse(statusCode, headers, null, response);
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                unexpectedResponse(statusCode, headers, throwable, errorResponse);
            }

            private void unexpectedResponse(int statusCode, Header[] headers, Throwable throwable, Object response) {
                Throwable useThrowable = throwable;
                if (null == useThrowable) {
                    useThrowable = new JSONException(
                            "Unexpected response (" +
                                    ((null != response) ? response.getClass().getName() : "null") +
                                    ")");
                }
                onFailure(statusCode,
                        headers,
                        (null != response) ? response.toString() : null,
                        useThrowable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                responseHandler.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }
}


