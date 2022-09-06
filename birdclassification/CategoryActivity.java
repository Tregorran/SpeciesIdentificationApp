package com.example.birdclassification;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

//import com.example.birdclassification.databinding.ActivityMainBinding;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

public class CategoryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView titleTV;
    private LinearLayout cardLayoutList;
    private ImageButton backIB, settingsIB, searchIB, favouriteIB, favouriteBIB, listIB, imageListIB;
    private Button recentB, categoryB;
    private ImageButton galleryIB, cameraIB;

    private SaveDataClass saveDataObj;
    private BucketClass bucketDataObj;
    private SavePreferencesClass preferenceObj;

    private String curCategory = "recent";

    //initialise views
    private void initViews() {
        toolbar = findViewById(R.id.myToolBar);
        titleTV = findViewById(R.id.title_TextView);
        backIB = findViewById(R.id.back_ImageButton);
        settingsIB = findViewById(R.id.settings_ImageButton);
        searchIB = findViewById(R.id.search_ImageButton);
        favouriteIB = findViewById(R.id.favourite_ImageButton);
        favouriteBIB = findViewById(R.id.favouriteB_ImageButton);
        listIB = findViewById(R.id.list_ImageButton);
        imageListIB = findViewById(R.id.imageList_ImageButton);
        cardLayoutList = findViewById(R.id.layout_list);

        recentB = findViewById(R.id.recent_button);
        categoryB = findViewById(R.id.categories_button);
        galleryIB = findViewById(R.id.gallery_button);
        cameraIB = findViewById(R.id.camera_button);
    }

    private void SetUpToolBar(){
        setSupportActionBar(toolbar);
        SetToolBarInvisible();
        SetImageListType();
    }

    private void SetToolBarInvisible(){
        backIB.setVisibility(View.GONE);
        searchIB.setVisibility(View.GONE);
        favouriteIB.setVisibility(View.GONE);
        favouriteBIB.setVisibility(View.GONE);
        titleTV.setText("Bird Classifier");
        titleTV.setTextColor(Color.WHITE);
    }

    // set the type of image list depending on what the user has last selected
    private void SetImageListType(){
        if (preferenceObj.imageListType.equals("imageList")){
            listIB.setVisibility(View.GONE);
            imageListIB.setVisibility(View.VISIBLE);
            preferenceObj.SavePreferenceData(getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE), preferenceObj);
        } else if (preferenceObj.imageListType.equals("list")){
            listIB.setVisibility(View.VISIBLE);
            imageListIB.setVisibility(View.GONE);
            preferenceObj.SavePreferenceData(getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE), preferenceObj);
        }
    }

    //set Tool bar when on category option
    private void ToolBarCategory(String googleQuery, boolean favouriteCategory){
        backIB.setVisibility(View.VISIBLE);
        backIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardLayoutList.removeAllViews();
                try {
                    SetUpBirdListCat();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                titleTV.setText("Bird Classifier");
                SetToolBarInvisible();
            }
        });

        if (googleQuery == null){
            searchIB.setVisibility(View.GONE);
        } else {
            searchIB.setVisibility(View.VISIBLE);
            searchIB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = Uri.parse("https://www.google.com/search?q=" + googleQuery);
                    Intent gSearchIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(gSearchIntent);
                }
            });
        }
    }

    //loads the data and bucket from storage
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
        } else {
            findViewById(R.id.background_ConstraintLayout).setBackgroundColor(ContextCompat.getColor(this, R.color.green_3));
        }
    }

    private void initialise(){
        initViews();
        LoadData();
        ApplyTheme();
        SetUpToolBar();
        SetUpBirdList("recent");
        onRecent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        initialise();

        //upon opening app, prompts user to allow camera and gallery access
        hasStoragePermission(1);

        settingsIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
            }
        });

        recentB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRecent();
                SetToolBarInvisible();
                SetUpBirdList("recent");
            }
        });

        categoryB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCategory();
                SetToolBarInvisible();
                try {
                    SetUpBirdListCat();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //navigates to crop activity and opens camera
        cameraIB.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CategoryActivity.this, CropActivity.class);
                i.putExtra("type", "camera");
                i.putExtra("from", "main");
                startActivity(i);
            }
        });

        //navigates to crop activity and opens gallery
        galleryIB.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CategoryActivity.this, CropActivity.class);
                i.putExtra("type", "gallery");
                i.putExtra("from", "main");
                startActivity(i);
            }
        });

        listIB.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                preferenceObj.imageListType = "imageList";
                SetImageListType();
                SetUpBirdList(curCategory);
            }
        });

        imageListIB.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                preferenceObj.imageListType = "list";
                SetImageListType();
                SetUpBirdList(curCategory);
            }
        });
    }

    //control the colour of button when selecting Recent option
    private void onRecent(){
        categoryB.setBackgroundColor(recentB.getContext().getResources().getColor(R.color.green_2));
        recentB.setBackgroundColor(recentB.getContext().getResources().getColor(R.color.green_3));
    }

    //control the colour of button when selecting category option
    private void onCategory(){
        recentB.setBackgroundColor(recentB.getContext().getResources().getColor(R.color.green_2));
        categoryB.setBackgroundColor(recentB.getContext().getResources().getColor(R.color.green_3));
    }

    //determine when to show settings icon
    private void SetSettingsIB(String categoryKey){
        if (categoryKey.equals("recent")) {
            settingsIB.setVisibility(View.VISIBLE);
        } else {
            settingsIB.setVisibility(View.GONE);
        }
    }

    //create list of keys
    private String[] CreateKeysList(Set<String> keySet){
        String[] keys = new String[keySet.size()-1];
        int counter = 0;
        for (String key: keySet) {
            if (!key.equals("size")) {
                keys[counter] = key;
                counter += 1;
            }
        }
        return keys;
    }

    //set card type and initialise
    private View SetCardPreferences(){
        View cardView = null;
        if (preferenceObj.imageListType.equals("imageList")){
            cardView = getLayoutInflater().inflate(R.layout.image_card, null, false);
        } else if (preferenceObj.imageListType.equals("list")){
            cardView = getLayoutInflater().inflate(R.layout.image_list_card, null, false);
        }

        if (preferenceObj.themeType.equals("light")){
            cardView.findViewById(R.id.select_LinearLayout).setBackground(ContextCompat.getDrawable(this, R.drawable.round_card_light));
            cardView.findViewById(R.id.card_CardView).setBackground(ContextCompat.getDrawable(this, R.drawable.round_card_light));
            cardView.findViewById(R.id.editCard_ImageButton).setBackground(ContextCompat.getDrawable(this, R.drawable.round_card_light));
        } else {
            cardView.findViewById(R.id.select_LinearLayout).setBackground(ContextCompat.getDrawable(this, R.drawable.round_card_dark));
            cardView.findViewById(R.id.card_CardView).setBackground(ContextCompat.getDrawable(this, R.drawable.round_card_dark));
            cardView.findViewById(R.id.editCard_ImageButton).setBackground(ContextCompat.getDrawable(this, R.drawable.round_card_dark));
        }
        return cardView;
    }

    // Edit card and go to resultsActivity
    private void EditCardResults(String curKey){
        Intent i = new Intent(CategoryActivity.this, ResultsActivity.class);
        i.putExtra("resPath", saveDataObj.data.get(curKey).imageLocation);
        i.putExtra("chosen", String.valueOf(saveDataObj.data.get(curKey).indexChosen));
        i.putExtra("key", curKey);

        if (bucketDataObj.buckets.get("favourites").get(curKey) != null){
            i.putExtra("favourite", "true");
        } else {
            i.putExtra("favourite", "false");
        }
        startActivity(i);
    }

    // sets up cards in layoutlist for the category
    private void SetUpBirdList(String categoryKey){
        SetSettingsIB(categoryKey); // when to show the settings icon
        SetImageListType(); // shows the image list type
        curCategory = categoryKey;

        cardLayoutList.removeAllViews();//reset list

        //retrieve the keys in the reverse order, so they appear by last added
        String[] keys = CreateKeysList(bucketDataObj.buckets.get(categoryKey).keySet());

        //if no cards are saved, display guide
        if ((keys.length-1) < 0){
            View intoView = getLayoutInflater().inflate(R.layout.intro_guide, null, false);
            cardLayoutList.addView(intoView);
        }

        //reverse through the keys list, so the cards appear by last added
        int counter = 0;
        for (int i = keys.length-1; i >= 0; i--){

            //only display 20 cards in the most recent category
            if (categoryKey.equals("recent")){
                if (counter > 20){
                    break;
                }
                counter ++;
            }

            String curKey = keys[i];

            // Initialise type of card and set colour depending on theme
            View cardView = SetCardPreferences();

            // retrieve the UI view components of the card
            TextView speciesTV = (TextView) cardView.findViewById(R.id.species_TextView);
            TextView dateTV = (TextView) cardView.findViewById(R.id.date_TextView);

            ImageButton editCardIB = (ImageButton) cardView.findViewById(R.id.editCard_ImageButton);
            ImageButton deleteIB = (ImageButton) cardView.findViewById(R.id.delete_ImageButton);
            ImageButton shareIB = (ImageButton) cardView.findViewById(R.id.share_ImageButton);
            ImageButton favouriteBIB = (ImageButton) cardView.findViewById(R.id.favouriteB_ImageButton);
            ImageButton favouriteIB = (ImageButton) cardView.findViewById(R.id.favourite_ImageButton);
            Button selectImageB = (Button)cardView.findViewById(R.id.selectImage_Button);

            //set the information of the card
            int indexChosen = saveDataObj.data.get(curKey).indexChosen;

            //set the text and image for the card
            editCardIB.setImageURI(Uri.parse(saveDataObj.data.get(curKey).imageLocation));
            speciesTV.setText(saveDataObj.data.get(curKey).speciesList.get(indexChosen).replaceAll("_", " "));
            dateTV.setText(curKey.split("\\.")[0]);

            //set the favourite button for each card
            if (bucketDataObj.buckets.get("favourites").get(curKey) != null) {
                favouriteIB.setVisibility(View.VISIBLE);
                favouriteBIB.setVisibility(View.GONE);
            } else {
                favouriteBIB.setVisibility(View.VISIBLE);
                favouriteIB.setVisibility(View.GONE);
            }

            ImageButton finalFavouriteBIB = favouriteBIB;
            ImageButton finalFavouriteIB = favouriteIB;
            String finalKey = curKey;

            //edit the card to choose different prediction in ResultsActivity
            selectImageB.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View view) {
                    EditCardResults(curKey);
                }
            });

            //delete card
            View finalCardView = cardView;
            deleteIB.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View view) {
                    DeleteCard(finalKey, finalCardView);
                }
            });

            //share button
            shareIB.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View view) {
                    String speciesName = saveDataObj.data.get(curKey).speciesList.get(indexChosen).replaceAll("_", " ");
                    ShareImage(editCardIB, speciesName);
                }
            });

            //toggle favourite button
            favouriteIB.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View view) {
                    finalFavouriteBIB.setVisibility(View.VISIBLE);
                    finalFavouriteIB.setVisibility(View.GONE);
                    SharedPreferences myUserPrefs = getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE);
                    bucketDataObj.FavouriteCard(finalKey, false, myUserPrefs);
                }
            });

            //toggle favourite button
            favouriteBIB.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View view) {
                    finalFavouriteBIB.setVisibility(View.GONE);
                    finalFavouriteIB.setVisibility(View.VISIBLE);
                    SharedPreferences myUserPrefs = getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE);
                    bucketDataObj.FavouriteCard(finalKey, true, myUserPrefs);
                }
            });

            //add card to list to display
            cardLayoutList.addView(cardView);
        }
    }

    private void ShareImage(ImageButton editCardIB, String speciesName){
        Drawable mDrawable = editCardIB.getDrawable();
        Bitmap mBitmap = ((BitmapDrawable) mDrawable).getBitmap();

        String path = MediaStore.Images.Media.insertImage(getContentResolver(), mBitmap, "Image Description", null);
        Uri uri = Uri.parse(path);

        String text = "I found a " + speciesName + "!";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Image..."));
    }

    private void DeleteCard(String key, View cardView){
        SharedPreferences myUserPrefs = getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE);

        int index = saveDataObj.data.get(key).indexChosen;
        String catNum = saveDataObj.data.get(key).categoryNumList.get(index);
        bucketDataObj.RemoveEntry(key, catNum, myUserPrefs);
        saveDataObj.RemoveEntry(key, myUserPrefs);

        cardLayoutList.removeView(cardView);
    }

    private void SetUpBirdListCat() throws IOException {
        listIB.setVisibility(View.GONE);
        imageListIB.setVisibility(View.GONE);
        settingsIB.setVisibility(View.VISIBLE);


        cardLayoutList.removeAllViews(); //reset list

        //favourites
        View cardView = getLayoutInflater().inflate(R.layout.categorycard, null, false);
        TextView categoryTV = (TextView) cardView.findViewById(R.id.category_TextView);
        TextView itemsTV = (TextView) cardView.findViewById(R.id.items_TextView);
        Button categoryButton = (Button) cardView.findViewById(R.id.category_Button);
        categoryTV.setText("Favourites");
        itemsTV.setText(bucketDataObj.buckets.get("favourites").get("size") + " Items");

        if (preferenceObj.themeType.equals("light")){
            GradientDrawable sha = (GradientDrawable) categoryButton.getBackground();
            sha.setColor(ContextCompat.getColor(this, R.color.darkgrey));
        } else {
            GradientDrawable sha = (GradientDrawable) categoryButton.getBackground();
            sha.setColor(ContextCompat.getColor(this, R.color.green_3));
        }

        cardLayoutList.addView(cardView);

        categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardLayoutList.removeAllViews();
                titleTV.setText("Favourites");
                SetUpBirdList("favourites");
                ToolBarCategory(null, true);
            }
        });

        //other categories
        InputStream inputStream = getResources().openRawResource(R.raw.classes);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String eachline = bufferedReader.readLine();
        int counter = 0;
        while (eachline != null) {
            cardView = getLayoutInflater().inflate(R.layout.categorycard, null, false);
            categoryTV = (TextView) cardView.findViewById(R.id.category_TextView);
            itemsTV = (TextView) cardView.findViewById(R.id.items_TextView);
            categoryButton = (Button) cardView.findViewById(R.id.category_Button);

            if (preferenceObj.themeType.equals("light")){
                GradientDrawable sha = (GradientDrawable) categoryButton.getBackground();
                sha.setColor(ContextCompat.getColor(this, R.color.grey));
            } else {
                GradientDrawable sha = (GradientDrawable) categoryButton.getBackground();
                sha.setColor(ContextCompat.getColor(this, R.color.green_2));
            }

            String categoryName = eachline.split("\\.")[1].replaceAll("_", " ");
            if (preferenceObj.catDisplayType.equals("alphabet")){
                String[] catNameArray = categoryName.split(" ");
                categoryName = catNameArray[catNameArray.length-1] + ",";
                for (int i = 0; i < catNameArray.length-1; i ++){
                    categoryName = categoryName + " " + catNameArray[i];
                }
            }
            categoryTV.setText(categoryName);

            int curCount = counter;
            String ItemsText = bucketDataObj.buckets.get(Integer.toString(curCount)).get("size");
            if (ItemsText.equals("0")){
                ItemsText = "-";
            } else {
                ItemsText = ItemsText + " Items";
            }
            itemsTV.setText(ItemsText);

            String finalEachline = eachline.split("\\.")[1].replaceAll("_", " ");
            categoryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cardLayoutList.removeAllViews();
                    titleTV.setText(finalEachline);
                    SetUpBirdList(String.valueOf(curCount));
                    ToolBarCategory(finalEachline, false);
                }
            });

            counter += 1;
            cardLayoutList.addView(cardView);

            eachline = bufferedReader.readLine();
        }
    }

    private boolean hasStoragePermission(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                requestPermissions(new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, requestCode);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}