package com.example.birdclassification;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataEntryClass {
    String imageLocation; // Location of the image
    int indexChosen; // Of the top 5 results which do they save
    ArrayList<String> speciesList; // Species names of the top 5 predictions
    ArrayList<String> speciesProbsList; // Probabilities of top 5 predictions
    ArrayList<String> categoryNumList; //Category numbers of the top 5 predictions e.g. 0 = category 0

    public void SetImage(String imageLocation){
        this.imageLocation = imageLocation;
    }

    public void SetIndexChosen(int indexChosen){
        this.indexChosen = indexChosen;
    }

    public void SetSpeciesList(ArrayList<String> speciesList){
        this.speciesList = speciesList;
    }

    public void SetSpeciesProbs(ArrayList<String> speciesProbsList){
        this.speciesProbsList = speciesProbsList;
    }

    public void SetCategoryNumList(ArrayList<String>  categoryNumList){
        this.categoryNumList = categoryNumList;
    }
}
