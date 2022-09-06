package com.example.birdclassification;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.birdclassification.ml.BirdModel;
import com.example.birdclassification.ml.BoundingModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ClassifierClass {

    // Convert image to byteBuffer and normalise image
    public ByteBuffer BitmapToByteBuffer(Bitmap image, int width, int height) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * width * height * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        // get 1D array of width * height pixels in image
        int[] intValues = new int[width * height];
        image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

        // iterate over pixels and extract R, G, and B values and add to bytebuffer.
        int pixel = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int val = intValues[pixel++]; // RGB
                byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255));
                byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255));
                byteBuffer.putFloat((val & 0xFF) * (1.f / 255));
            }
        }
        return byteBuffer;
    }

    // pass image through classification model
    public float[] ClassifyImageBird(Bitmap image, Context context, int imageSize) {
        try {
            BirdModel model = BirdModel.newInstance(context);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = BitmapToByteBuffer(image, imageSize, imageSize);
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            BirdModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();

            // Releases model resources if no longer used.
            model.close();
            return confidences;
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return null;
    }

    // pass image through model to get bounding box prediction
    public float[] ClassifyImageBounding(Bitmap image, Context context, int imageSize) {
        try {
            BoundingModel model = BoundingModel.newInstance(context);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = BitmapToByteBuffer(image, imageSize, imageSize);
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            BoundingModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();

            // Releases model resources if no longer used.
            model.close();
            return confidences;
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return null;
    }

    //get the indexes of the top predictions in the array
    public int[] GetMaxPostions(float[] confidences){
        int[] maxIndexes = {0,0,0,0,0};

        for (int i = 0; i < confidences.length; i++) {
            for (int j = 0; j < maxIndexes.length; j++){
                if (confidences[i] > confidences[maxIndexes[j]]){
                    //move positions up 1
                    for(int k = maxIndexes.length-2; k >= j; k--){
                        maxIndexes[k+1] = maxIndexes[k];
                    }
                    maxIndexes[j] = i;
                    break;
                }
            }
        }
        return maxIndexes;
    }
}
