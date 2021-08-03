package com.example.imageprocessing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
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

        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image_chooser();
            }
        });
    }

    private void image_chooser() {

//        if (ActivityCompat.checkSelfPermission(FilterImage.this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(FilterImage.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
//            return;
//        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");

        Log.d("INTENT", String.valueOf(RESULT_OK));
        startActivityForResult(intent, SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uri = new ArrayList<>();
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            ClipData clipData = data.getClipData();

            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); ++i) {
                    Uri selectedImageUri = clipData.getItemAt(i).getUri();
                    uri.add(String.valueOf(selectedImageUri));
                    Log.d("URI: ", String.valueOf(selectedImageUri));
                }
            }
        }

        result = new ArrayList<>();

        for (int i = 0; i < uri.size(); ++i) {
            Uri imageUri = Uri.parse(uri.get(i));
            if (imageUri != null) {
                Log.d("StoragePrediction", "Output_uri: " + imageUri);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Mat selected_image = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
                Utils.bitmapToMat(bitmap, selected_image);

                selected_image = objectDetectorClass.recognizePhoto(selected_image);

                result.add(objectDetectorClass.getObjectName(selected_image));
            }
        }
        for (int i = 0; i < result.size(); ++i) {
            for (int j = 0; j < result.get(i).size(); ++j) {
                Log.d("Label: " + i, result.get(i).get(j));
            }
        }
    }
}