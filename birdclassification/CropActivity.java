package com.example.birdclassification;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
//import com.example.croppersample.R;
//import com.example.croppersample.databinding.FragmentCameraBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import android.graphics.Rect;
import android.widget.Toast;

public class CropActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button classBtn;
    private ImageButton cropBtn, retakeBtn, prevBtn;
    private ImageView pictureIV;


    private String intentChosen = null;
    private Uri tempImgUri;
    private String tempFilePath;
    private Uri originalImgUri;
    private Uri resultImgUri;
    private Rect resultWindow;

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_PICK_CODE = 1001;
    private static final int IMAGE_CAPTURE_CODE = 1002;
    private static final int IMAGE_CROP_CODE = 1003;

    SavePreferencesClass preferenceObject;

    private void initViews(){
        toolbar = findViewById(R.id.myToolBar);
        pictureIV = findViewById(R.id.ivCroppedImage);
        cropBtn = findViewById(R.id.crop_button);
        classBtn = findViewById(R.id.classify_button);
        retakeBtn = findViewById(R.id.retake_button);
        SetUpToolBar();
    }

    public void SetUpToolBar(){
        setSupportActionBar(toolbar);
        prevBtn = findViewById(R.id.back_ImageButton);
        findViewById(R.id.settings_ImageButton).setVisibility(View.GONE);
        findViewById(R.id.search_ImageButton).setVisibility(View.GONE);
        findViewById(R.id.favourite_ImageButton).setVisibility(View.GONE);
        findViewById(R.id.favouriteB_ImageButton).setVisibility(View.GONE);
        findViewById(R.id.list_ImageButton).setVisibility(View.GONE);
        findViewById(R.id.imageList_ImageButton).setVisibility(View.GONE);
    }

    private void LoadData(){
        preferenceObject = new SavePreferencesClass();
        preferenceObject.LoadPreferenceData(getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE));
    }

    private void initalise(){
        initViews();
        LoadData();
        ApplyTheme();
    }

    private void ApplyTheme(){
        if (preferenceObject.themeType.equals("light")){
            findViewById(R.id.background_FrameLayout).setBackgroundColor(Color.WHITE);
        } else {
            findViewById(R.id.background_FrameLayout).setBackgroundColor(ContextCompat.getColor(this, R.color.green_3));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        initalise();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //last chosen to use camera or gallery
            intentChosen = extras.getString("type");
            if (intentChosen.equals("camera")){
                CameraActivity();
            } else if (intentChosen.equals("gallery")){
                GalleryActivity();
            // user used backbutton from resultsActivity
            } else if (intentChosen.equals("none")){
                CropDataClass cropDataObj = new CropDataClass();
                cropDataObj.LoadCropData(getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE));
                intentChosen = cropDataObj.intentChosen;
                originalImgUri = Uri.parse(cropDataObj.originalImageUri);
                resultImgUri = Uri.parse(cropDataObj.resultImageUri);
                resultWindow = cropDataObj.resultWindow;
                pictureIV.setImageURI(resultImgUri);
            }
        } else {
            Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
            startActivity(i);
        }

        prevBtn.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
                startActivity(i);
            }
        });

        cropBtn.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                onActivityResult(IMAGE_CROP_CODE, RESULT_OK,null);
            }
        });

        classBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                CropDataClass cropDataObj = new CropDataClass(intentChosen, originalImgUri.toString(), resultImgUri.toString(), resultWindow);
                cropDataObj.SaveCropData(getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE));
                Intent i = new Intent(CropActivity.this, ResultsActivity.class);
                i.putExtra("resPath", resultImgUri.toString());
                i.putExtra("chosen", "0");
                i.putExtra("favourite", "false");
                startActivity(i);
            }
        });

        retakeBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (intentChosen.equals("camera")){
                    CameraActivity();
                } else if (intentChosen.equals("gallery")){
                    GalleryActivity();
                } else {
                    Log.v("Error", "Incorrect choice");
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void CameraActivity(){
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            ContextWrapper context = new ContextWrapper(getApplicationContext());
            // Continue only if the File was successfully created
            if (photoFile != null) {
                tempImgUri = FileProvider.getUriForFile(context,
                        "com.example.birdclassification.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri);
                startActivityForResult(takePictureIntent, IMAGE_CAPTURE_CODE);
            }
        } else {
            Log.v("shape", "error1");
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    //create place for where the image should be stored when taking photo
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        tempFilePath = image.getAbsolutePath();
        return image;
    }

    // opens gallery to select a photo
    public void GalleryActivity(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                //permission not granted, request it
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                //show popup for runtime permission
                requestPermissions(permissions, PERMISSION_CODE);
            } else {
                //permission already granted
                pickImageFromGallery();
            }
        } else {
            //system os is less then marshmallow
            pickImageFromGallery();
        }
    }

    //user can now pick image from the gallery
    private void pickImageFromGallery() {
        //intent to pick image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    //handle result of runtime permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission was granted
                    pickImageFromGallery();
                } else {
                    //permission was denied
                    Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        // image from camera
        if (requestCode == IMAGE_CAPTURE_CODE) {
            Bitmap bitmap = BitmapFactory.decodeFile(tempFilePath);
            if (bitmap != null){
                ContextWrapper context = new ContextWrapper(getApplicationContext());
                try {
                    bitmap = rotateImageIfRequired(context, bitmap, tempImgUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Save image to gallery
                originalImgUri = saveImageToExternal(bitmap);

                //get bounding box suggestion
                Rect suggestionRect = ClassifyBox(originalImgUri);

                Toast.makeText(CropActivity.this,
                        "Please crop the image so that only the bird is visible", Toast.LENGTH_LONG).show();

                CropImage.activity(originalImgUri)
                        .setInitialCropWindowRectangle(suggestionRect)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle("Crop")
                        .start(this);

            } else {
                //User exited camera
                //if user last pressed crop_code, don't do anything
                if (pictureIV.getDrawable() == null){
                    Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
                    startActivity(i);
                }
            }
        // image from gallery
        } else if (requestCode == IMAGE_PICK_CODE) {
                if (resultCode == RESULT_OK && data != null) {
                originalImgUri = data.getData();
                Rect suggestionRect = ClassifyBox(originalImgUri);
                Toast.makeText(CropActivity.this,
                        "Please crop the image so that only the bird is visible", Toast.LENGTH_LONG).show();
                CropImage.activity(originalImgUri)
                        .setInitialCropWindowRectangle(suggestionRect)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle("Crop")
                        .start(this);
            } else {
                    if (pictureIV.getDrawable() == null){
                        Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
                        startActivity(i);
                    }
                }
        //Recrop the image
        } else if (requestCode == IMAGE_CROP_CODE) {
                Toast.makeText(CropActivity.this,
                        "Please crop the image so that only the bird is visible", Toast.LENGTH_LONG).show();
                CropImage.activity(originalImgUri)
                        .setInitialCropWindowRectangle(resultWindow)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle("Crop")
                        .start(this);
        //get results image from cropping
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (data != null) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                resultWindow = result.getCropRect();
                if (resultCode == RESULT_OK) {
                    resultImgUri = result.getUri();
                    pictureIV.setImageURI(resultImgUri);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    error.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //certain Android phones rotate the images
    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    //Save image to gallery
    private Uri saveImageToExternal(Bitmap finalBitmap) {
        File Dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!Dir.exists()) {
            Dir.mkdirs();
        }

        String imgName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "Image-"+ imgName +".jpg";

        File imageFile = new File (Dir, fname);
        if (imageFile.exists ())
            imageFile.delete ();
        try {
            FileOutputStream out = new FileOutputStream(imageFile);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            ContextWrapper context = new ContextWrapper(getApplicationContext());
            MediaScannerConnection.scanFile(context, new String[]{imageFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                }
            });
            return Uri.fromFile(imageFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Uri.fromFile(imageFile);
    }

    //Predict cropping window suggestion from image
    private Rect ClassifyBox(Uri resultImage){
        //get bitmap of image
        Bitmap bitmap = null;
        Context context = getApplicationContext();
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver() , resultImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // resize image to input shape of model
        int imageHeight = bitmap.getHeight();
        int imageWidth = bitmap.getWidth();
        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, false);

        // predict the bounding boxes of the image
        ClassifierClass classifierClass = new ClassifierClass();
        float[] confidences = classifierClass.ClassifyImageBounding(bitmap, context, 224);

        // rescale the bounding box coordinates
        int x = Math.round(confidences[0]*imageWidth);
        int y = Math.round(confidences[1]*imageHeight);
        int width = Math.round(confidences[2]*imageWidth);
        int height = Math.round(confidences[3]*imageHeight);

        // Android-Image-Cropper requires Rect(int left, int top, int right, int bottom)
        Rect newRect = new Rect(x, y, x+width, y+height);

        return newRect;
    }
}