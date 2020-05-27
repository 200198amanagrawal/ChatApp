package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseUser m_currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(m_currentUser!=null)
        {
            startMainActivity();
        }
    }
    private void startMainActivity()
    {
        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
    }
}
