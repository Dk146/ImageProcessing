package com.example.imageprocessing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;

public class FilterImage extends AppCompatActivity {
    private Button select_image;
    private ImageView img_view;
    private objectDetectorClass objectDetectorClass;
    int SELECT_PICTURE = 200;
    ArrayList<String> uri = new ArrayList<>();
    ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Filter", "Image");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_image);

        select_image = findViewById(R.id.select_image_2);
        try {
            // input size is 300 for this model
            objectDetectorClass = new objectDetectorClass(getAssets(),"ssd_mobilenet.tflite","labelmap.txt", 300);
            Log.d("MainActivity", "Model is successfully loaded");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Uri folder;
        folder = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Log.d("Path", String.valueOf(folder));

        String []projection = {
                MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };
        String orderby = MediaStore.Video.Media.DATE_TAKEN;
        Cursor cursor = getContentResolver().query(folder, projection,null,
                            null, orderby + " DESC");

        int ColumnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        String absPath;
        uri = new ArrayList<>();
        while (cursor.moveToNext()){
            absPath = cursor.getString(ColumnIndexData);
            uri.add(absPath);
        }

        predict();
    }

    /*
    private void image_chooser() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");

        Log.d("INTENT", String.valueOf(RESULT_OK));
        startActivityForResult(intent, SELECT_PICTURE);
    }

     */

    public void predict() {
        result = new ArrayList<>();

        for (int i = 0; i < uri.size(); ++i) {

            Uri imageUri = Uri.parse(uri.get(i));
            if (imageUri != null) {
                Log.d("StoragePrediction", "Output_uri: " + imageUri);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    Log.d("StoragePrediction", "success");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("StoragePrediction", "fail");
                }

                /*
                Mat selected_image = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
                Utils.bitmapToMat(bitmap, selected_image);

                selected_image = objectDetectorClass.recognizePhoto(selected_image);

                result.add(objectDetectorClass.getObjectName(selected_image));
                */
            }
        }
        for (int i = 0; i < result.size(); ++i) {
            for (int j = 0; j < result.get(i).size(); ++j) {
                Log.d("Label: " + i, result.get(i).get(j));
            }
        }
    }

}