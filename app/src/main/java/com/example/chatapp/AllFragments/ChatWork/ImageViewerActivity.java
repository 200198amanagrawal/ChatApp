package com.example.chatapp.AllFragments.ChatWork;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.example.chatapp.R;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageView imageView;
    private String imageURl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView=findViewById(R.id.image_viewer);
        imageURl=getIntent().getStringExtra("url");

        Picasso.get().load(imageURl).into(imageView);
    }
}
