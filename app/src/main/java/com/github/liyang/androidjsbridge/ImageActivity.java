package com.github.liyang.androidjsbridge;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * 2017/12/4
 */
public class ImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ImageView imageView = findViewById(R.id.iv_image);
        String imageUrl = getIntent().getStringExtra("imgUrl");
        Picasso.with(this).load(imageUrl).into(imageView);
    }
}
