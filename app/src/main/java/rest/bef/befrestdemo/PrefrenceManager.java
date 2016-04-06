package rest.bef.befrestdemo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hojjatimani on 4/3/2016 AD.
 */
public class PrefrenceManager {

    public static final String SHARED_PREFERENCES_NAME = "PREFS";
    public static final String PREF_NEEDS_SIGN_UP = "PREF_NEEDS_SIGN_UP";
    public static final String PREF_USER_ID = "PREF_USER_ID";
    public static final String PREF_CONTACTS = "PREF_CONTACTS";
    public static final String PREF_IS_FIRST_RUN_VERSION2 = "PREF_IS_FIRST_RUN_VERSION2";
    public static final String PREF_REGULAR_MSG_TTL = "PREF_REGULAR_MSG_TTL";
    public static final String PREF_TOPIC_MSG_TTL = "PREF_TOPIC_MSG_TTL";

    static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    static void saveInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putInt(key, value).commit();
    }

    static void saveString(Context context, String key, String value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(key, value).commit();
    }

    static void saveFloat(Context context, String key, float value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putFloat(key, value).commit();
    }

    static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(key, value).commit();
    }

    static void saveLong(Context context, String key, long value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putLong(key, value).commit();
    }
}
