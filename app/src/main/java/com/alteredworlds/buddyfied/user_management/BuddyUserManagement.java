package com.alteredworlds.buddyfied.user_management;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.alteredworlds.buddyfied.BuildConfig;
import com.alteredworlds.buddyfied.Constants;
import com.alteredworlds.buddyfied.R;
import com.alteredworlds.buddyfied.Settings;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by twcgilbert on 24/09/2014.
 */
public class BuddyUserManagement implements UserManagement {

    private final static String LOG_TAG = BuddyUserManagement.class.getSimpleName();

    private final static String sMagicTokenUrl = "api/get_nonce/?controller=user&method=";
    private final static String sBuddyfiedRegisterUserURL = "api/user/buddypress_register/";
    private final static String sBuddyfiedUpdateProfileURL = "api/user/xprofile_multi_update/";
    private final static String sBuddyfiedGenerateCookieURL = "api/user/generate_auth_cookie/";

    private final AsyncHttpClient mClient;
    private String mAuthCookie;

    public BuddyUserManagement(Context context) {
        mClient = new AsyncHttpClient();
        String userAgent = context.getString(R.string.app_name) + "/" + BuildConfig.VERSION_NAME + "/" + BuildConfig.VERSION_CODE +
                " (" + android.os.Build.MODEL + "; Android " + Build.VERSION.RELEASE + ")";
        mClient.setUserAgent(userAgent);
    }

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
            final HashMap<String, String> profileData,
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
            final HashMap<String, String> profileData,
            final JsonHttpResponseHandler responseHandler) {
        grabAuthCookieIfNeededForUser(context,
                user,
                password,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        //OK, we should now have the authCookie token in responseString
                        updateProfileUsingAuthCookie(context, responseString, profileData, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Log.i(LOG_TAG, "updateProfileForUser result: " + response.toString());
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

    private void grabAuthCookieIfNeededForUser(
            final Context context,
            final String user,
            final String password,
            final JsonHttpResponseHandler responseHandler) {
        if (!TextUtils.isEmpty(mAuthCookie)) {   // we already have it...
            responseHandler.onSuccess(Constants.RESULT_OK, null, mAuthCookie);
        } else {
            // we need to get the blasted thing...
            // first we're going to need an NONCE token
            grabNonce(context,
                    "generate_auth_cookie",
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            //OK, we should now have the nonce token in responseString
                            generateAuthCookie(context, user, password, responseString, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, String response) {
                                    Log.i(LOG_TAG, "generateAuthCookie result: " + response);
                                    // pass cookie back via overload of onSuccess that returns a String
                                    // but it is also cached in mAuthCookie
                                    responseHandler.onSuccess(statusCode, headers, response);
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                    Log.e(LOG_TAG, "generateAuthCookie failed");
                                    responseHandler.onFailure(statusCode, headers, responseString, throwable);
                                }
                            });
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(LOG_TAG, "grabAuthCookieIfNeededForUser failed to grabNonceForUser ");
                            responseHandler.onFailure(statusCode, headers, responseString, throwable);
                        }
                    });
        }
    }

    private void generateAuthCookie(
            final Context context,
            final String user,
            final String password,
            final String nonce,
            final JsonHttpResponseHandler responseHandler) {
        mAuthCookie = null;
        //
        StringBuilder urlStr = new StringBuilder(Settings.getBuddySite(context));
        urlStr.append(sBuddyfiedGenerateCookieURL);
        urlStr.append("?nonce=");
        urlStr.append(escapedString(nonce));
        urlStr.append("&username=");
        urlStr.append(escapedString(user));
        urlStr.append("&password=");
        urlStr.append(escapedString(password));
        //StringBuilder urlStr = new StringBuilder("http://requestb.in/oa5t1roa");
        mClient.post(context, urlStr.toString(), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i(LOG_TAG, "generateAuthCookie result: " + response.toString());
                try {
                    if (0 == "ok".compareTo(response.getString("status"))) {
                        mAuthCookie = (String) response.get("cookie");
                        if (!TextUtils.isEmpty(mAuthCookie)) {
                            // pass cookie back via overload of onSuccess that returns a String
                            responseHandler.onSuccess(statusCode, headers, mAuthCookie);
                        } else {
                            unexpectedResponse(this, statusCode, headers, null, response);
                        }
                    } else {
                        unexpectedResponse(this, statusCode, headers, null, response);
                    }
                } catch (Exception e) {
                    unexpectedResponse(this, statusCode, headers, e, response);
                }
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

    private void updateProfileUsingAuthCookie(Context context,
                                              String authCookie,
                                              final HashMap<String, String> profileData,
                                              final JsonHttpResponseHandler responseHandler) {
        StringBuilder urlStr = new StringBuilder(Settings.getBuddySite(context));
        urlStr.append(sBuddyfiedUpdateProfileURL);
        urlStr.append("?cookie=");
        urlStr.append(escapedString(authCookie));
        if (null != profileData) {
            for (Map.Entry<String, String> entry : profileData.entrySet()) {
                String escapedKey = escapedString(entry.getKey());
                String escapedValue = escapedString(entry.getValue());
                urlStr.append("&");
                urlStr.append(escapedKey);
                urlStr.append("=");
                urlStr.append(escapedValue);
            }
        }
        mClient.post(context, urlStr.toString(), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i(LOG_TAG, "updateProfileUsingAuthCookie result: " + response.toString());
                try {
                    if (0 == "ok".compareTo(response.getString("status"))) {
                        responseHandler.onSuccess(statusCode, headers, response);
                    } else {
                        unexpectedResponse(this, statusCode, headers, null, response);
                    }
                } catch (Exception e) {
                    unexpectedResponse(this, statusCode, headers, e, response);
                }
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

    private void registerUser(Context context,
                              String nonce,
                              String user,
                              String password,
                              String email,
                              HashMap<String, String> profileData,
                              final JsonHttpResponseHandler responseHandler) {
        StringBuilder urlStr = new StringBuilder(Settings.getBuddySite(context));
        urlStr.append(sBuddyfiedRegisterUserURL);
        urlStr.append("?username=");
        urlStr.append(escapedString(user));
        urlStr.append("&nonce=");
        urlStr.append(escapedString(nonce));
        urlStr.append("&email=");
        urlStr.append(escapedString(email));
        urlStr.append("&password=");
        urlStr.append(escapedString(password));
        if (null != profileData) {
            for (Map.Entry<String, String> entry : profileData.entrySet()) {
                String escapedKey = escapedString(entry.getKey());
                String escapedValue = escapedString(entry.getValue());
                urlStr.append("&");
                urlStr.append(escapedKey);
                urlStr.append("=");
                urlStr.append(escapedValue);
            }
        }
        mClient.post(context, urlStr.toString(), null, new JsonHttpResponseHandler() {
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

    private String escapedString(String value) {
        String retVal = null;
        try {
            retVal = java.net.URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //WTF is the point of this?
        }
        return retVal;
    }
}


