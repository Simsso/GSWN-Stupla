package com.timodenk.gswnstupla;

import android.content.Context;
import android.content.SharedPreferences;


class LocalStorage {
    private static final String ELEMENT_ID_KEY = "elementId", FIRST_VISIBLE_ITEM_KEY = "firstVisibleItem";

    private Context context;

    LocalStorage(Context context) {
        this.context = context;
    }

    // stores an element id
    void saveElementId(int elementId) {
        setInt(ELEMENT_ID_KEY, elementId);
    }

    // loads the stored element id
    int loadElementId() {
        return getInt(ELEMENT_ID_KEY, -1);
    }

    // stores an element id
    void saveFirstVisibleItem(int firstVisibleItem) {
        setInt(FIRST_VISIBLE_ITEM_KEY, firstVisibleItem);
    }

    // loads the stored element id
    int loadFirstVisibleItem() {
        return getInt(FIRST_VISIBLE_ITEM_KEY, 0);
    }


    // loads an integer by its key
    private int getInt(String key, int defaultValue){
        SharedPreferences settings = context.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        return settings.getInt(key, defaultValue);
    }

    private void setInt(String key, int value) {
        SharedPreferences settings = context.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.apply();
    }
}
