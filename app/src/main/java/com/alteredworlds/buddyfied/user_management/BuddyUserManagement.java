package com.alteredworlds.buddyfied.user_management;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alteredworlds.buddyfied.Settings;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by twcgilbert on 24/09/2014.
 */
public class BuddyUserManagement implements UserManagement {

    private final static String LOG_TAG = BuddyUserManagement.class.getSimpleName();

    private final static String sMagicTokenUrl = "api/get_nonce/?controller=user&method=";
    private final static String sBuddyfiedRegisterUserURL = "api/user/buddypress_register";
    private final static String sBuddyfiedUpdateProfileURL = "%@api/user/xprofile_multi_update/?cookie=%@";
    private final static String sBuddyfiedGenerateCookieURL = "%@api/user/generate_auth_cookie/?nonce=%@&username=%@&password=%@";

    private final AsyncHttpClient mClient = new AsyncHttpClient();

    @Override
    public void cancel(Context context) {
        mClient.cancelRequests(context, true);
    }

    @Override
    public void registerNewUser(
            final Context context,
            final String user,
            final String password,
            final String email,
            final HashMap<String, Object> profileData,
            final JsonHttpResponseHandler responseHandler) {
        grabNonce(context,
                "register",
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        //OK, we should now have the nonce token in responseString
                        registerUser(context, responseString, user, password, email, profileData, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Log.i(LOG_TAG, "registerUser result: " + response.toString());

                                responseHandler.onSuccess(statusCode, headers, response);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                responseHandler.onFailure(statusCode, headers, responseString, throwable);
                            }
                        });
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        responseHandler.onFailure(statusCode, headers, responseString, throwable);
                    }
                });
    }

    @Override
    public void updateProfileForUser(
            final Context context,
            final String user,
            final String password,
            final HashMap<String, Object> profileData,
            final JsonHttpResponseHandler responseHandler) {

    }

    private void registerUser(Context context,
                              String nonce,
                              String user,
                              String password,
                              String email,
                              HashMap<String, Object> profileData,
                              final JsonHttpResponseHandler responseHandler) {
        //username=%@&nonce=%@&email=%@&password=%@
        String urlStr = Settings.getBuddySite(context) + sBuddyfiedRegisterUserURL;
        RequestParams params = new RequestParams();
        params.put("username", user);
        params.put("nonce", nonce);
        params.put("email", email);
        params.put("password", password);
        if (null != profileData) {
            for (Map.Entry<String, Object> entry : profileData.entrySet()) {
                params.put(entry.getKey(), entry.getValue());
            }
        }
        mClient.post(context, urlStr, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i(LOG_TAG, "registerUser result: " + response.toString());

                responseHandler.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                unexpectedResponse(this, statusCode, headers, null, response);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                unexpectedResponse(this, statusCode, headers, null, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                responseHandler.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                unexpectedResponse(this, statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                unexpectedResponse(this, statusCode, headers, throwable, errorResponse);
            }
        });
    }

    private void grabNonce(Context context,
                           String method,
                           final JsonHttpResponseHandler responseHandler) {
        // FIRST we need the magic token, retrieved via an initial call
        String urlStr = Settings.getBuddySite(context) + sMagicTokenUrl + method;
        mClient.get(urlStr, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i(LOG_TAG, "nonce result: " + response.toString());
                try {
                    String nonce = (String) response.get("nonce");
                    if (!TextUtils.isEmpty(nonce)) {
                        // pass nonce back via overload of onSuccess that returns a String
                        responseHandler.onSuccess(statusCode, headers, nonce);
                    } else {
                        unexpectedResponse(this, statusCode, headers, null, response);
                    }
                } catch (Exception e) {
                    unexpectedResponse(this, statusCode, headers, e, response);
                }
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                unexpectedResponse(this, statusCode, headers, throwable, errorResponse);
            }

            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                unexpectedResponse(this, statusCode, headers, null, response);
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                unexpectedResponse(this, statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                responseHandler.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    private static void unexpectedResponse(
            JsonHttpResponseHandler handler, int statusCode, Header[] headers, Throwable throwable, Object response) {
        Throwable useThrowable = throwable;
        if (null == useThrowable) {
            useThrowable = new JSONException(
                    "Unexpected response (" +
                            ((null != response) ? response.getClass().getName() : "null") +
                            ")");
        }
        handler.onFailure(statusCode,
                headers,
                (null != response) ? response.toString() : null,
                useThrowable);
    }
}


