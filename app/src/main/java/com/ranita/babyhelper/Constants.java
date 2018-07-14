package com.ranita.babyhelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Constants {
    public static final String UPLOAD_FILE_RECEIVER_NAME = "com.ranita.babyhelper.uploadservice.broadcast.status";

    public static final String FILE_TYPE = "video/mp4";

    public static int REQ_CHOOSE_VIDEO = 10001;
    public static int REQ_STORAGE_PERMISSION = 10002;

    public static final String INPUT_IP = "INPUT_IP";
    public static final String INPUT_USER = "INPUT_USER";

    public static String getSharedPref(String key, Context ctx){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPref.getString(key, "");
    }

    public static void setSharedPref(String key, String value, Context ctx){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }
}
