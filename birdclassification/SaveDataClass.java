package com.example.birdclassification;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class SaveDataClass {
    //date key, entry information
    Map<String, DataEntryClass> data;

    //Load in all the data
    public void LoadData(SharedPreferences mPrefs){
        String json = mPrefs.getString("Data", null);
        Type type = new TypeToken<Map<String, DataEntryClass>>() {}.getType();
        Gson gson = new Gson();
        data = gson.fromJson(json, type);
        if (data == null){
            data = new HashMap<String, DataEntryClass>();
        }
    }

    //Save all the data
    public void SaveData(SharedPreferences mPrefs){
        if (data == null){
            data = new HashMap<String, DataEntryClass>();
        }
        SharedPreferences.Editor editor = mPrefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(data);
        editor.putString("Data", json);
        editor.apply();
    }

    //add a new data entry to save
    public void AddEntry(String key, DataEntryClass newEntry,SharedPreferences myUserPrefs){
        data.put(key, newEntry);
        SaveData(myUserPrefs);
    }

    //remove a data entry
    public void RemoveEntry(String key, SharedPreferences myUserPrefs){
        data.remove(key);
        SaveData(myUserPrefs);
    }
}
