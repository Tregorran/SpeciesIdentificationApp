package com.example.birdclassification;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    TextView titleTV;
    ImageButton prevBtn, favouriteBBtn, favouriteBtn;
    ImageButton removeBtn, saveBtn;
    ImageView classIV, compareIV;
    TextView resultTV, similarTV, hyperTV;

    int imageSize = 224;
    String googleQuery = null;

    String saveImage;
    int saveIndexChosen;
    ArrayList<String> saveSpeciesList = new ArrayList<>();
    ArrayList<String> saveCategoryNumList = new ArrayList<>();
    ArrayList<String> saveSpeciesProbs = new ArrayList<>();

    boolean favourite;

    private SaveDataClass saveDataObj;
    private BucketClass bucketDataObj;
    String key = null;

    SavePreferencesClass preferenceObj;

    private void setFavouriteButton(boolean favourite){
        this.favourite = favourite;
        if (favourite == true){
            favouriteBtn.setVisibility(View.VISIBLE);
            favouriteBBtn.setVisibility(View.GONE);
        } else {
            favouriteBBtn.setVisibility(View.VISIBLE);
            favouriteBtn.setVisibility(View.GONE);
        }
    }

    public void SetUpToolBar() {
        setSupportActionBar(toolbar);

        prevBtn = findViewById(R.id.back_ImageButton);
        favouriteBtn = findViewById(R.id.favourite_ImageButton);
        favouriteBBtn = findViewById(R.id.favouriteB_ImageButton);

        favouriteBtn.setVisibility(View.GONE);
        findViewById(R.id.settings_ImageButton).setVisibility(View.GONE);
        findViewById(R.id.search_ImageButton).setVisibility(View.GONE);
        findViewById(R.id.list_ImageButton).setVisibility(View.GONE);
        findViewById(R.id.imageList_ImageButton).setVisibility(View.GONE);
    }

    private void initViews() {
        SetUpToolBar();

        toolbar = findViewById(R.id.myToolBar);
        titleTV = findViewById(R.id.title_TextView);
        classIV = findViewById(R.id.result_ImageView);
        compareIV = findViewById(R.id.compare_ImageView);
        resultTV = findViewById(R.id.prediction_TextView);
        similarTV = findViewById(R.id.similarity_TextView);
        hyperTV = findViewById(R.id.hyperlink_TextView);
        removeBtn = findViewById(R.id.remove_ImageButton);
        saveBtn = findViewById(R.id.save_ImageButton);
    }

    private void LoadData(){
        saveDataObj = new SaveDataClass();
        saveDataObj.LoadData(getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE));

        bucketDataObj = new BucketClass();
        bucketDataObj.LoadBucket(getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE));

        preferenceObj = new SavePreferencesClass();
        preferenceObj.LoadPreferenceData(getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE));
    }

    private void ApplyTheme(){
        if (preferenceObj.themeType.equals("light")){
            findViewById(R.id.background_ConstraintLayout).setBackgroundColor(Color.WHITE);
            resultTV.setTextColor(getResources().getColor(R.color.grey));
            similarTV.setTextColor(getResources().getColor(R.color.grey));
            classIV.setBackgroundColor(getResources().getColor(R.color.grey));
            compareIV.setBackgroundColor(getResources().getColor(R.color.grey));

        } else {
            findViewById(R.id.background_ConstraintLayout).setBackgroundColor(ContextCompat.getColor(this, R.color.green_3));
            resultTV.setTextColor(Color.WHITE);
            similarTV.setTextColor(Color.WHITE);
            classIV.setBackgroundColor(getResources().getColor(R.color.green_2));
            compareIV.setBackgroundColor(getResources().getColor(R.color.green_2));
        }
    }

    // get list of 200 bird species names
    private ArrayList<String> ReadClasses() throws IOException {
        ArrayList<String> classes = new ArrayList<String>();
        InputStream inputStream = getResources().openRawResource(R.raw.classes);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String eachline = bufferedReader.readLine();
        while (eachline != null) {
            classes.add(eachline.split("\\.")[1]);
            eachline = bufferedReader.readLine();
        }
        return classes;
    }

    private void initalise(){
        initViews();
        LoadData();
        ApplyTheme();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        initalise();

        // retrieve image
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            saveImage = extras.getString("resPath");
            saveIndexChosen = Integer.valueOf(extras.getString("chosen"));
            key = extras.getString("key");
            setFavouriteButton(Boolean.parseBoolean(extras.getString("favourite")));
        }

        // display new image
        classIV.setImageURI(Uri.parse(saveImage));

        // Having selected an image from the CategoryActivity
        if (key != null){
            titleTV.setText("Classification Info");

            if (bucketDataObj.buckets.get("favourites").get(key) != null){
                setFavouriteButton(true);
            }

            saveSpeciesList = saveDataObj.data.get(key).speciesList;
            saveCategoryNumList = saveDataObj.data.get(key).categoryNumList;
            saveSpeciesProbs = saveDataObj.data.get(key).speciesProbsList;

            resultTV.setText("Prediction: " + saveDataObj.data.get(key).speciesList.get(saveIndexChosen).replaceAll("_"," "));

            similarTV.setText("Similarity: " + saveDataObj.data.get(key).speciesProbsList.get(saveIndexChosen));

            SetUpBottomSheet(saveDataObj.data.get(key).speciesList, saveDataObj.data.get(key).speciesProbsList);

            //set comparison image.
            SetUpComparisonImage(saveDataObj.data.get(key).speciesList.get(saveIndexChosen).toLowerCase());

            googleQuery = saveDataObj.data.get(key).speciesList.get(saveIndexChosen).replaceAll("_"," ");
        // from the CropActivity
        } else {
            titleTV.setText("Classification Results");

            ClassifyBird(Uri.parse(saveImage));
        }

        String finalKey = key;
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (finalKey == null){
                    Intent i = new Intent(ResultsActivity.this, CropActivity.class);
                    i.putExtra("type", "none");
                    startActivity(i);
                } else {
                    Intent i = new Intent(ResultsActivity.this, CategoryActivity.class);
                    startActivity(i);
                }
            }
        });

        favouriteBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                setFavouriteButton(false);
            }
        });

        favouriteBBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                setFavouriteButton(true);
            }
        });

        hyperTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.google.com/search?q=" + googleQuery);
                Intent gSearchIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(gSearchIntent);
            }
        });

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (key != null) {
                    RemoveEntry();
                }
                Intent i = new Intent(ResultsActivity.this, CategoryActivity.class);
                startActivity(i);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveData();

                if (key != null) {
                    RemoveEntry();
                }

                Intent i = new Intent(ResultsActivity.this, CategoryActivity.class);
                startActivity(i);
            }
        });
    }

    private void RemoveEntry(){
        SharedPreferences myUserPrefs = getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE);

        int index = saveDataObj.data.get(key).indexChosen;
        String catNum = saveDataObj.data.get(key).categoryNumList.get(index);
        bucketDataObj.RemoveEntry(key, catNum, myUserPrefs);
        saveDataObj.RemoveEntry(key, myUserPrefs);
    }

    // classify bird and displays results
    private void ClassifyBird(Uri resultImage) {
        try {
            ArrayList<String> classes = ReadClasses();

            //resizing image to classification size
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultImage);
            bitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false);

            // Classifies the image
            ClassifierClass classifierClass = new ClassifierClass();
            float[] confidences = classifierClass.ClassifyImageBird(bitmap, getApplicationContext(), imageSize);

            //retrieves the indexes of the top prediction
            int[] maxIndexes = classifierClass.GetMaxPostions(confidences);

            // Display results
            String topSpeciesName = classes.get(maxIndexes[saveIndexChosen]).replaceAll("_"," ");
            String topSpeciesProb = String.format("%.2f", confidences[maxIndexes[saveIndexChosen]] * 100);
            resultTV.setText("Prediction: " + topSpeciesName);
            similarTV.setText("Similarity: " +  topSpeciesProb + "%");

            //Set up the attributes for the DataEntryClass to be saved
            for (int i = 0; i < maxIndexes.length; i++) {
                saveSpeciesList.add(classes.get(maxIndexes[i]));
                saveSpeciesProbs.add(String.format("%.2f", confidences[maxIndexes[i]] * 100));
                saveCategoryNumList.add(String.valueOf(maxIndexes[i]));
            }

            // display the top 4 predictions in the drag-up window
            SetUpBottomSheet(saveSpeciesList, saveSpeciesProbs);

            //set comparison image.
            SetUpComparisonImage(saveSpeciesList.get(saveIndexChosen).toLowerCase());

            googleQuery = classes.get(maxIndexes[saveIndexChosen]).replaceAll("_"," ");

        } catch (Exception e) {
            System.out.println("Classification not working");
        }
    }

    private void SetUpComparisonImage(String compareName){
        Context context = getApplication().getApplicationContext();
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier(compareName, "raw", context.getPackageName());
        compareIV.setImageDrawable(getResources().getDrawable(resourceId));
    }

    private void SetUpBottomSheet(ArrayList<String> speciesList, ArrayList<String> speciesProbs) {
        //Initialise drag-up behavour of bottom sheet
        FrameLayout sheet = findViewById(R.id.sheet);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
        behavior.setPeekHeight(365);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        View entryView;
        TextView numberingTV;
        TextView nameTV;
        TextView percentTV;
        ProgressBar progressBar;
        Button selectB;

        LinearLayout layoutList = (LinearLayout) findViewById(R.id.layout_list_results);
        for (int i = 0; i < 5; i++) {
            entryView = getLayoutInflater().inflate(R.layout.bottom_sheet_entries, null, false);
            numberingTV = (TextView) entryView.findViewById(R.id.numbering_TextView);
            nameTV = (TextView) entryView.findViewById(R.id.name_TextView);
            percentTV = (TextView) entryView.findViewById(R.id.percent_TextView);
            progressBar = (ProgressBar) entryView.findViewById(R.id.progressBar_View);
            selectB = (Button) entryView.findViewById(R.id.select_Button);

            numberingTV.setText((i + 1) + ".");
            nameTV.setText(speciesList.get(i).replaceAll("_"," "));

            String percent = speciesProbs.get(i);

            // set up the progress bar colours
            percentTV.setText(percent + "%");

            progressBar.setProgress(Math.round(Float.valueOf(percent)));
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            if (Float.parseFloat(percent) <= 20){
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
            } else if (Float.parseFloat(percent) > 20 && Float.parseFloat(percent) <= 45){
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
            } else {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
            }

            int finalI = i;
            selectB.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View view) {
                    if (key == null) {
                        Intent i = new Intent(ResultsActivity.this, ResultsActivity.class);
                        i.putExtra("resPath", saveImage);
                        i.putExtra("chosen", String.valueOf(finalI));
                        i.putExtra("favourite", String.valueOf(favourite));
                        startActivity(i);
                    } else {
                        Intent i = new Intent(ResultsActivity.this, ResultsActivity.class);
                        i.putExtra("resPath", saveImage);
                        i.putExtra("chosen", String.valueOf(finalI));
                        i.putExtra("key", key);
                        i.putExtra("favourite", String.valueOf(favourite));
                        startActivity(i);
                    }
                }
            });

            layoutList.addView(entryView);
        }
    }

    private void SaveData(){
        DataEntryClass newDataEntry = new DataEntryClass();
        SharedPreferences myUserPrefs = getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE);

        String dateKey = new SimpleDateFormat("dd/MM/yyyy.hh:mm:ss").format(new Date());

        //adding entry to category list
        bucketDataObj.AddEntryToBucket(saveCategoryNumList.get(saveIndexChosen), dateKey, myUserPrefs);

        //adding entry to recently added
        bucketDataObj.AddEntryToBucket("recent", dateKey, myUserPrefs);

        //adding entry to favourites
        if (favourite == true){
            bucketDataObj.AddEntryToBucket("favourites", dateKey, myUserPrefs);
        }

        newDataEntry.SetImage(saveImage);
        newDataEntry.SetIndexChosen(saveIndexChosen);
        newDataEntry.SetSpeciesList(saveSpeciesList);
        newDataEntry.SetCategoryNumList(saveCategoryNumList);
        newDataEntry.SetSpeciesProbs(saveSpeciesProbs);
        saveDataObj.AddEntry(dateKey, newDataEntry, myUserPrefs);
    }
}