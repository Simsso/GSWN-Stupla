package com.timodenk.gswnstupla;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Denk on 14/11/15.
 */
public class LocalStorage {
    // stores an element id
    public static void saveElementId(Context context, int elementId) {
        SharedPreferences settings = context.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("elementId", elementId);
        editor.apply();
    }

    // loads the stored element id
    public static int loadElementId(Context context) {
        SharedPreferences settings = context.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        return settings.getInt("elementId", -1);
    }
}
