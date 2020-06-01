package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    private String recieverUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        recieverUserId=getIntent().getExtras().get("visitUserId").toString();
        Toast.makeText(this, "User Id "+recieverUserId, Toast.LENGTH_SHORT).show();
    }
}
