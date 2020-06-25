package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
       setContentView(R.layout.activity_splash_screen);
        Thread thread=new Thread(){
            @Override
            public void run() {
                try {
                    sleep(4000);
                }
                catch (Exception e)
                {
                    e.getStackTrace();
                }
                finally {
                    Intent intent=new Intent(SplashScreen.this,MainActivity.class);
                    startActivity(intent);
                }
            }
        };
        thread.start();
    }
}
