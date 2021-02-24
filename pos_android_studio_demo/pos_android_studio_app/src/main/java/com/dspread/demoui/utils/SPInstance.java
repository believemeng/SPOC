package com.dspread.demoui.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.dspread.xpos.QPOSService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
*@date:2020/8/24
*@author:Qianmeng Chen
*@description:存储本地信息
*/
public class SPInstance {
    public static final String DSPREAD_PREFS = "dspread_prefs";
    public static final String UPDATED_VERSION = "updated_version";
    public static final String TOKEN = "token";
    public static final String TOKEN_EXPIRY_DATE = "token_expiry_date";
    public static final String IS_LOGGED_IN = "is_logged_in";
    public static final String USER_EMAIL = "user_email";
    public static final String USER_ID = "user_id";
    public static final String SERVER_DOMAIN = "user_login_saved_server";
    public static final String TERMINAL_ID = "terminal_id";

    private static SPInstance instance;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public static synchronized SPInstance getInstance(Context context) {
        if (instance == null) {
            instance = new SPInstance(context);
        }
        return instance;
    }

    private SPInstance(Context context) {
        prefs = context.getSharedPreferences(DSPREAD_PREFS, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }


    // Attendee updated their data. for offline design
    public void saveUpdatedVersion(int versionNo) {
        editor.putInt(UPDATED_VERSION, versionNo).apply();
    }

    // token pack
    public void saveToken(String token) {
        if(token != null && !token.equals("")){
            editor.putString(TOKEN, "Bearer "+token).apply();
        }else{
            editor.putString(TOKEN, token).apply();
        }
    }

    public String getToken() {
        return prefs.getString(TOKEN, "");
    }

    public void saveTerminalId(String terminalId) {
        editor.putString(TERMINAL_ID, terminalId).apply();
    }

    public String getTerminalId(){
        return prefs.getString(TERMINAL_ID, "");
    }

    public long getTokenExpiryDate() {
        return prefs.getLong(TOKEN_EXPIRY_DATE, 0);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(IS_LOGGED_IN, false);
    }

    // USER_EMAIL
    public void saveUserEmail(String email) {
        editor.putString(USER_EMAIL, email).apply();
    }

    public String getUserEmail() {
        return prefs.getString(USER_EMAIL, "");
    }


}
