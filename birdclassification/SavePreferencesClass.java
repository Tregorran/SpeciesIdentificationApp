package com.example.birdclassification;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class SavePreferencesClass {
    String imageListType; //card list or large cards
    String catDisplayType; //standard or alphabetical
    String themeType; // dark or light theme

    //Load the data from storage
    public void LoadPreferenceData(SharedPreferences mPrefs){
        String json = mPrefs.getString("Preferences", null);
        Type type = new TypeToken<SavePreferencesClass>() {}.getType();
        Gson gson = new Gson();
        SavePreferencesClass loadedPreferenceData = gson.fromJson(json, type);

        if (loadedPreferenceData == null){
            loadedPreferenceData = new SavePreferencesClass();
        }

        if (loadedPreferenceData.imageListType == null){
            loadedPreferenceData.imageListType = "imageList";
        }
        if (loadedPreferenceData.catDisplayType == null){
            loadedPreferenceData.catDisplayType = "alphabet";
        }
        if (loadedPreferenceData.themeType == null){
            loadedPreferenceData.themeType = "light";
        }

        this.imageListType = loadedPreferenceData.imageListType;
        this.catDisplayType = loadedPreferenceData.catDisplayType;
        this.themeType = loadedPreferenceData.themeType;
    }

    //Save the preference data
    public void SavePreferenceData(SharedPreferences shared, SavePreferencesClass loadedPreferenceData){
        SharedPreferences.Editor editor = shared.edit();

        Gson gson = new Gson();
        String json = gson.toJson(loadedPreferenceData);
        editor.putString("Preferences", json);
        editor.apply();
    }
}
