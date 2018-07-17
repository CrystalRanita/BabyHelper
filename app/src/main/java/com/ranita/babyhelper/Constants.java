package com.ranita.babyhelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.ranita.babyhelper.Position;

public class Constants {
    public static final String UPLOAD_FILE_RECEIVER_NAME = "com.ranita.babyhelper.uploadservice.broadcast.status";

    public static final String FILE_TYPE = "video/mp4";

    public static int REQ_CHOOSE_VIDEO = 10001;
    public static int REQ_STORAGE_PERMISSION = 10002;

    public static final String INPUT_IP = "INPUT_IP";
    public static final String INPUT_USER = "INPUT_USER";

    public static final String G_LEFT = "G_LEFT";
    public static final String G_TOP = "G_TOP";
    public static final String G_RIGHT = "G_RIGHT";
    public static final String G_BOTTOM = "G_BOTTOM";

    public static final String R_LEFT = "R_LEFT";
    public static final String R_TOP = "R_TOP";
    public static final String R_RIGHT = "R_RIGHT";
    public static final String R_BOTTOM = "R_BOTTOM";

    public static final String B_LEFT = "B_LEFT";
    public static final String B_TOP = "B_TOP";
    public static final String B_RIGHT = "B_RIGHT";
    public static final String B_BOTTOM = "B_BOTTOM";

    public static final String RGB_SET_ALL = "RGB_SET_ALL";

    public static void setSharedPref(String key, String value, Context ctx){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getSharedPref(String key, Context ctx){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPref.getString(key, "");
    }

    public static void setIntSharedPref(String key, int value, Context ctx){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getIntSharedPref(String key, Context ctx){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPref.getInt(key, -1);
    }

    public static void setBoolSharedPref(String key, boolean isSet, Context ctx){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, isSet);
        editor.commit();
    }

    public static Boolean getBoolSharedPref(String key, Context ctx){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPref.getBoolean(key, false);
    }

    public static void setPosition(Context ctx, String type, int _left, int _top, int _right, int _bottom) {
        switch (type) {
            case "R":
                setIntSharedPref(R_LEFT, _left, ctx);
                setIntSharedPref(R_TOP, _top, ctx);
                setIntSharedPref(R_RIGHT, _right, ctx);
                setIntSharedPref(R_BOTTOM, _bottom, ctx);
                break;
            case "G":
                setIntSharedPref(G_LEFT, _left, ctx);
                setIntSharedPref(G_TOP, _top, ctx);
                setIntSharedPref(G_RIGHT, _right, ctx);
                setIntSharedPref(G_BOTTOM, _bottom, ctx);
                break;
            case "B":
                setIntSharedPref(B_LEFT, _left, ctx);
                setIntSharedPref(B_TOP, _top, ctx);
                setIntSharedPref(B_RIGHT, _right, ctx);
                setIntSharedPref(B_BOTTOM, _bottom, ctx);
                break;
            default:
                Toast.makeText(ctx, "Unknown position type: " + type, Toast.LENGTH_SHORT).show();
        }
    }

    public static Position getPosition(Context ctx, String type) {
        Position result_position = null;
        switch (type) {
            case "R":
                result_position = new Position(
                    getIntSharedPref(R_LEFT, ctx),
                    getIntSharedPref(R_TOP, ctx),
                    getIntSharedPref(R_RIGHT, ctx),
                    getIntSharedPref(R_BOTTOM, ctx)
                );
                break;
            case "G":
                result_position = new Position(
                    getIntSharedPref(G_LEFT, ctx),
                    getIntSharedPref(G_TOP, ctx),
                    getIntSharedPref(G_RIGHT, ctx),
                    getIntSharedPref(G_BOTTOM, ctx)
                );
                break;
            case "B":
                result_position = new Position(
                    getIntSharedPref(B_LEFT, ctx),
                    getIntSharedPref(B_TOP, ctx),
                    getIntSharedPref(B_RIGHT, ctx),
                    getIntSharedPref(B_BOTTOM, ctx)
                );
                break;
            default:
                Toast.makeText(ctx, "Unknown position type: " + type, Toast.LENGTH_SHORT).show();
                result_position = null;
        }
        Log.i("Constants", "Type: " + type
                + ", Position Left: " + result_position.left()
                + ", top: " + result_position.top()
                + ", right: " + result_position.right()
                + ", bottom: " + result_position.bottom());
        return result_position;
    }

    public static void resetAllPotition(Context ctx) {
        setPosition(ctx, "R", -1, -1, -1, -1);
        setPosition(ctx, "G", -1, -1, -1, -1);
        setPosition(ctx, "B", -1, -1, -1, -1);
    }
}