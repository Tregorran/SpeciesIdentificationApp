package com.example.birdclassification;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

public class CropDataClass {
    String intentChosen; //user last chose camera or gallery
    String originalImageUri; // location of original image
    String resultImageUri; // location of cropped image
    Rect resultWindow; // the last cropping window used

    // for loading the class without assigning values
    CropDataClass(){}

    CropDataClass(String intentChosen, String originalImageUri, String resultImageUri, Rect resultWindow)
    {
        this.intentChosen = intentChosen;
        this.originalImageUri = originalImageUri;
        this.resultImageUri = resultImageUri;
        this.resultWindow = resultWindow;
    }

    //Save the cropping data
    public void SaveCropData(SharedPreferences mPrefs){
        CropDataClass cropDataClass = new CropDataClass(intentChosen, originalImageUri, resultImageUri, resultWindow);

        SharedPreferences.Editor editor = mPrefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(cropDataClass);
        editor.putString("CropData", json);
        editor.apply();
    }

    //load the cropping data
    public void LoadCropData(SharedPreferences mPrefs){
        String json = mPrefs.getString("CropData", null);
        Type type = new TypeToken<CropDataClass>() {}.getType();
        Gson gson = new Gson();
        CropDataClass cropDataClass = gson.fromJson(json, type);

        intentChosen = cropDataClass.intentChosen;
        originalImageUri = cropDataClass.originalImageUri;
        resultImageUri = cropDataClass.resultImageUri;
        resultWindow = cropDataClass.resultWindow;
    }
}
