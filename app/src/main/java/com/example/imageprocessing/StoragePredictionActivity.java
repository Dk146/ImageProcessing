package com.example.imageprocessing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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

public class StoragePredictionActivity extends AppCompatActivity {
    private Button select_image;
    private ImageView img_view;
    private objectDetectorClass objectDetectorClass;
    int SELECT_PICTURE = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_prediction);

        select_image = findViewById(R.id.select_image);
        img_view = findViewById(R.id.image_view);

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
        Log.d("image_chooser","YES");

        Intent i = new Intent();

        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        Log.d("image_chooser","YES");

        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult","YES");
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("RESULT_OK", String.valueOf(RESULT_OK));
        Log.d("REQUESTCODE", String.valueOf(SELECT_PICTURE));
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                Log.d("Uri", String.valueOf(selectedImageUri));
                if (selectedImageUri != null) {
                    Log.d("StoragePrediction", "Output_uri: " + selectedImageUri);
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }

                    Mat selected_image = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
                    Utils.bitmapToMat(bitmap, selected_image);

                    selected_image = objectDetectorClass.recognizePhoto(selected_image);
                    Log.d("NO1","YES");

                    Bitmap bitmap1 = null;
                    bitmap1 = Bitmap.createBitmap(selected_image.cols(), selected_image.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(selected_image, bitmap);

                    img_view.setImageBitmap(bitmap1);
                    Log.d("NO","YES");
                }
            }
        }

    }
}