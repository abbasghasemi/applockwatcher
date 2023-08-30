package ghasemi.abbas.applockwatcher.builder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ghasemi.abbas.applockwatcher.ApplicationLoader;

public class TinyData {

    private static TinyData tinyData;
    private SharedPreferences sharedPreferences;

    public static TinyData getInstance() {
        if (tinyData == null) {
            tinyData = new TinyData(ApplicationLoader.context);
        }
        return tinyData;
    }


    private TinyData(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String s) {
        return sharedPreferences.getString(key, s);
    }

    public void putBool(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBool(String key) {
        return getBool(key, false);
    }

    public boolean getBool(String key, boolean value) {
        return sharedPreferences.getBoolean(key, value);
    }

    public long getLong(String key, long value) {
        return sharedPreferences.getLong(key, value);
    }


    public void putLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

}
