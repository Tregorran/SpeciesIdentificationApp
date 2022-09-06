package com.example.birdclassification;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class BucketClass {
    //buckets {'category', {'key', '1'}} pair
    Map<String, Map<String, String>> buckets;

    //Load all buckets
    public void LoadBucket(SharedPreferences mPrefs){
        String json = mPrefs.getString("BucketData", null);
        Type type = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
        Gson gson = new Gson();
        buckets = gson.fromJson(json, type);

        if (buckets == null){
            buckets = InitBucket();
        }
    }

    //save all buckets
    public void SaveBucket(SharedPreferences shared){
        if (buckets == null){
            buckets = new HashMap<String, Map<String, String>>();
        }
        SharedPreferences.Editor editor = shared.edit();

        Gson gson = new Gson();
        String json = gson.toJson(buckets);
        editor.putString("BucketData", json);
        editor.apply();
    }

    //initialise the size of the buckets
    private Map<String, Map<String, String>> InitBucket(){
        Map<String, Map<String, String>> categoryBuckets = new HashMap<String, Map<String, String>>();
        Map<String, String> initEntry;
        //initialise all species categories
        for (int i = 0; i < 200; i ++){
            initEntry = new HashMap<String, String>();
            initEntry.put("size", "0");
            categoryBuckets.put(Integer.toString(i), initEntry);
        }
        //initialise favourites
        initEntry = new HashMap<String, String>();
        initEntry.put("size", "0");
        categoryBuckets.put("favourites", initEntry);

        //initialise recent
        initEntry = new HashMap<String, String>();
        initEntry.put("size", "0");
        categoryBuckets.put("recent", initEntry);
        return categoryBuckets;
    }

    //remove an entry from the buckets
    public void RemoveEntry(String key, String catNum, SharedPreferences myUserPrefs){
        Map<String, String> entries;
        int size;
        if (buckets.get("favourites").get(key) != null){
            entries = buckets.get("favourites");
            size = Integer.valueOf(entries.get("size"));
            entries.put("size", String.valueOf(size-1));
            buckets.put("favourites", entries);
        }

        entries = buckets.get("recent");
        size = Integer.valueOf(entries.get("size")) ;
        entries.put("size", String.valueOf(size-1));
        buckets.put("recent", entries);

        entries = buckets.get(catNum);
        size = Integer.valueOf(entries.get("size"));
        entries.put("size", String.valueOf(size-1));
        buckets.put(catNum, entries);

        //removes it from category it is in
        buckets.get("favourites").remove(key);
        buckets.get("recent").remove(key);
        buckets.get(catNum).remove(key);

        SaveBucket(myUserPrefs);
    }

    //add to or remove from the favourite category
    public void FavouriteCard(String key, boolean changeFavourite, SharedPreferences myUserPrefs){
        Map<String, String> entries = buckets.get("favourites");
        if (changeFavourite == true){
            //add key to favourites category
            entries.put(key, "1");
            int size = Integer.valueOf(entries.get("size"));
            entries.put("size", String.valueOf(size+1));
            buckets.put("favourites", entries);
        } else {
            //remove key from favourites category
            buckets.get("favourites").remove(key);
            int size = Integer.valueOf(entries.get("size"));
            entries.put("size", String.valueOf(size-1));
        }

        SaveBucket(myUserPrefs);
    }

    //add datekeys to a bucket
    public void AddEntryToBucket(String newKey, String dateKey, SharedPreferences myUserPrefs){
        Map<String, String> newEntry = buckets.get(newKey);

        int size = Integer.valueOf(newEntry.get("size"));
        newEntry.put("size", Integer.toString(size+1));

        newEntry.put(dateKey, "1");
        buckets.put(newKey, newEntry);

        SaveBucket(myUserPrefs);
    }
}
