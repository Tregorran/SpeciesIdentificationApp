package com.example.birdclassification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    TextView titleTV;
    private ScrollView listSV;
    private LinearLayout cardLayoutList;
    private ImageButton backIB, settingsIB, searchIB, favouriteIB, favouriteBIB, listIB, imageListIB;
    private Button recentB, categoryB;
    private ImageButton galleryIB, cameraIB;
    private ImageView lightIV, darkIV;
    private RadioButton catNormalRB, catAlphaRB, lightRB, darkRB;

    SavePreferencesClass preferenceObj;

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
        listSV = findViewById(R.id.list_ScrollView);

        recentB = findViewById(R.id.recent_button);
        categoryB = findViewById(R.id.categories_button);
        galleryIB = findViewById(R.id.gallery_button);
        cameraIB = findViewById(R.id.camera_button);

        catNormalRB = findViewById(R.id.catNormal_RadioButton);
        catAlphaRB = findViewById(R.id.catAlpha_RadioButton);

        lightIV = findViewById(R.id.light_ImageView);
        darkIV = findViewById(R.id.dark_ImageView);

        lightRB =  findViewById(R.id.light_RadioButton);
        darkRB = findViewById(R.id.dark_RadioButton);
    }

    private void SetUpToolBar(){
        setSupportActionBar(toolbar);
        SetToolBarInvisible();
    }

    //loads the data and bucket from storage
    private void LoadData(){
        preferenceObj = new SavePreferencesClass();
        preferenceObj.LoadPreferenceData(getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE));
    }

    private void SetToolBarInvisible(){
        settingsIB.setVisibility(View.GONE);
        searchIB.setVisibility(View.GONE);
        favouriteIB.setVisibility(View.GONE);
        favouriteBIB.setVisibility(View.GONE);
        listIB.setVisibility(View.GONE);
        imageListIB.setVisibility(View.GONE);
        titleTV.setText("Settings");
        titleTV.setTextColor(Color.WHITE);
    }

    private void initialise(){
        initViews();
        LoadData();
        SetUpToolBar();
        ApplyTheme();
        initCatRadio();
        initThemeRadio();
    }

    // initialise radio button for category list
    private void initCatRadio(){
        if (preferenceObj.catDisplayType.equals("alphabet")) {
            catNormalRB.setChecked(false);
            catAlphaRB.setChecked(true);
        } else {
            catNormalRB.setChecked(true);
            catAlphaRB.setChecked(false);
        }
    }

    // initialise radio button for theme
    private void initThemeRadio(){
        if (preferenceObj.themeType.equals("light")) {
            lightRB.setChecked(true);
            darkRB.setChecked(false);
        } else {
            lightRB.setChecked(false);
            darkRB.setChecked(true);
        }
    }

    private void ApplyTheme(){
        if (preferenceObj.themeType.equals("light")){
            findViewById(R.id.background_ConstraintLayout).setBackgroundColor(Color.WHITE);
            findViewById(R.id.displayCat_LinearLayout).setBackground(ContextCompat.getDrawable(this, R.drawable.round_card_light));
            findViewById(R.id.theme_LinearLayout).setBackground(ContextCompat.getDrawable(this, R.drawable.round_card_light));
        } else {
            findViewById(R.id.background_ConstraintLayout).setBackgroundColor(ContextCompat.getColor(this, R.color.green_3));
            findViewById(R.id.displayCat_LinearLayout).setBackground(ContextCompat.getDrawable(this, R.drawable.round_card_dark));
            findViewById(R.id.theme_LinearLayout).setBackground(ContextCompat.getDrawable(this, R.drawable.round_card_dark));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initialise();

        catNormalRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                catAlphaRB.setChecked(false);
                preferenceObj.catDisplayType = "normal";
                preferenceObj.SavePreferenceData(getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE), preferenceObj);
            }
        });

        catAlphaRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                catNormalRB.setChecked(false);
                preferenceObj.catDisplayType = "alphabet";
                preferenceObj.SavePreferenceData(getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE), preferenceObj);
            }
        });

        // set image for dark theme
        InputStream imageStream = this.getResources().openRawResource(R.raw.dark);
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
        darkIV.setImageBitmap(bitmap);

        // set image for light theme
        imageStream = this.getResources().openRawResource(R.raw.light);
        bitmap = BitmapFactory.decodeStream(imageStream);
        lightIV.setImageBitmap(bitmap);

        // set to light theme
        lightRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                darkRB.setChecked(false);
                preferenceObj.themeType = "light";
                preferenceObj.SavePreferenceData(getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE), preferenceObj);
                ApplyTheme();
            }
        });

        // set to dark theme
        darkRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lightRB.setChecked(false);
                preferenceObj.themeType = "dark";
                preferenceObj.SavePreferenceData(getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE), preferenceObj);
                ApplyTheme();
            }
        });

        backIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
                startActivity(i);
            }
        });
    }
}