package com.example.imageprocessing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class FilterImage extends AppCompatActivity {
    private objectDetectorClass objectDetectorClass;
    int SELECT_PICTURE = 50;
    ArrayList<String> uri = new ArrayList<>();
    ArrayList<ArrayList<String>> result;
    RecyclerView recyclerView;
    GalleryAdapter galleryAdapter;
    Result instanceResult;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Filter", "Image");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_image);


        try {
            // input size is 300 for this model
            objectDetectorClass = new objectDetectorClass(getAssets(),"ssd_mobilenet.tflite","labelmap.txt", 300);
            Log.d("MainActivity", "Model is successfully loaded");
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        if(ContextCompat.checkSelfPermission(FilterImage.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(FilterImage.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
        }
        else{
            Uri folder;
            folder = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Log.d("Path", String.valueOf(folder));

            String []projection = {
                    MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            };
            String orderby = MediaStore.Video.Media.DATE_TAKEN;
            @SuppressLint("Recycle") Cursor cursor = getContentResolver()
                                .query(folder, projection,null,
                                null, orderby + " DESC");

            int ColumnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            String absPath;
            uri = new ArrayList<>();
            while (cursor.moveToNext()){
                absPath = cursor.getString(ColumnIndexData);
                uri.add(absPath);
            }
            instanceResult = Result.getInstance();
            instanceResult.setUri(uri);

            if(instanceResult.noneResult()) {
                predict1();
            }

            recyclerView = findViewById(R.id.gallery);

            galleryAdapter = new GalleryAdapter(this, uri, SELECT_PICTURE);
            recyclerView.setLayoutManager(new GridLayoutManager(this,4));
            recyclerView.setAdapter(galleryAdapter);

        }
    }

    private void predict1() {
        result = new ArrayList<>();
        for (int i = 0; i < uri.size() && i < SELECT_PICTURE; i++){
            int finalI = i;
            Glide.with(this)
                    .asBitmap()
                    .load(uri.get(i))
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if(resource != null){
                                predict2(resource, finalI);
                            }
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });

        }
    }

    public void predict2(Bitmap bitmap, int position) {

        Mat selected_image = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, selected_image);

        selected_image = objectDetectorClass.recognizePhoto(selected_image);

        result.add(objectDetectorClass.getObjectName(selected_image));


        for (int j = 0; j < result.get(position).size(); ++j) {
            Log.d("Label: " + position, result.get(position).get(j));
        }

        instanceResult.setResult(result);
    }

}