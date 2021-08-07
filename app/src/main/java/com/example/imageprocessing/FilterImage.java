package com.example.imageprocessing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import android.widget.AdapterView;


public class FilterImage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private objectDetectorClass objectDetectorClass;
    int SELECT_PICTURE = 20;
    ArrayList<String> uri = new ArrayList<>();
    ArrayList<ArrayList<String>> result;
    RecyclerView recyclerView;
    GalleryAdapter galleryAdapter;
    Result instanceResult;
    String spinnerLabel;

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
        else {
            Uri folder;
            folder = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Log.d("Path", String.valueOf(folder));

            String[] projection = {
                    MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            };
            String orderby = MediaStore.Video.Media.DATE_TAKEN;
            @SuppressLint("Recycle") Cursor cursor = getContentResolver()
                    .query(folder, projection, null,
                            null, orderby + " DESC");

            int ColumnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            String absPath;
            uri = new ArrayList<>();
            while (cursor.moveToNext()) {
                absPath = cursor.getString(ColumnIndexData);
                uri.add(absPath);
            }

            instanceResult = Result.getInstance();
            instanceResult.setUri(uri);

            if (instanceResult.noneResult()) {
                predict1();
            }

            recyclerView = findViewById(R.id.gallery);
            galleryAdapter = new GalleryAdapter(this, uri, SELECT_PICTURE);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
            recyclerView.setAdapter(galleryAdapter);

            Spinner spinner = findViewById(R.id.label_spinner);
            if (spinner != null) {
                spinner.setOnItemSelectedListener(this);
            }
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.labels_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            if (spinner != null) {
                spinner.setAdapter(adapter);
            }
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
        /*
        for (int j = 0; j < result.get(position).size(); ++j) {
            Log.d("Label: " + position, result.get(position).get(j));
        }
        */
        instanceResult.setResult(result);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        spinnerLabel = adapterView.getItemAtPosition(i).toString();
        Log.d("Spinner", spinnerLabel);
        if (spinnerLabel.equals("None") == false) {
            ArrayList<String> uri_temp = new ArrayList<>();
            Log.d("Spinner", spinnerLabel);
            Log.d("Instance_size", String.valueOf(instanceResult.uri.size()));
            for (int pos = 0; pos < instanceResult.uri.size() && pos < SELECT_PICTURE; ++pos) {

                for (int pos_result = 0; pos_result < instanceResult.getResult(pos).size(); ++pos_result) {

                    Log.d("URI_IMG", instanceResult.uri.get(pos));

                    if (spinnerLabel.toLowerCase().equals(instanceResult.getResult(pos).get(pos_result).toLowerCase())) {
                        uri_temp.add(instanceResult.uri.get(pos));
                        Log.d("URI_IMG", instanceResult.uri.get(pos));
                    }
                }
                Log.d( "SIZE",((String.valueOf(instanceResult.getResult(pos).size()))));
            }
            //recyclerView = findViewById(R.id.gallery);
            GalleryAdapter GA = new GalleryAdapter(this, uri_temp, uri_temp.size());
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
            recyclerView.setAdapter(GA);
        }
        else {
            recyclerView = findViewById(R.id.gallery);
            GalleryAdapter GA = new GalleryAdapter(this, instanceResult.uri, instanceResult.uri.size());
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
            recyclerView.setAdapter(GA);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

//    @Override
//    public Filter getFilter() {
//        galleryAdapter = new GalleryAdapter(this, uri_temp, SELECT_PICTURE);
//        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
//        recyclerView.setAdapter(galleryAdapter);
//        return null;
//    }
}