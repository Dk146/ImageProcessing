package com.example.imageprocessing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ImagePredictResult extends AppCompatActivity {

    Result result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict_result);
        result = Result.getInstance();

        Intent intent = getIntent();
        int index = intent.getIntExtra("index",0);

        ImageView imageView = (ImageView) findViewById(R.id.imageFull);
        Glide.with(this).load(result.getUri(index)).into(imageView);

        ArrayList<String> label = result.getResult(index);
        String mergeLabel = String.valueOf(label.size()) + ": ";
        for(int i = 0; i < label.size(); i++){
            mergeLabel = mergeLabel.concat(label.get(i) + "; ");
        }
        TextView textView = (TextView)findViewById(R.id.prediction);
        textView.setText(mergeLabel);
    }
}