package com.timodenk.gswnstupla;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by Denk on 14/11/15.
 */
public class LocalStorage {
    // stores an element id
    public static void saveElementId(Activity activity, int elementId) {
        SharedPreferences settings = activity.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("elementId", elementId);
        editor.apply();
    }

    // loads the stored element id
    public static int loadElementId(Activity activity) {
        SharedPreferences settings = activity.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        return settings.getInt("elementId", -1);
    }
}
