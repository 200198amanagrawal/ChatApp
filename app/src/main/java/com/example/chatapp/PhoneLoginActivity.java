package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button m_send_verCodeButton,m_verifyButton;
    private EditText m_OTP,m_phoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        initializeVariables();
        m_send_verCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_send_verCodeButton.setVisibility(View.INVISIBLE);
                m_phoneNumber.setVisibility(View.INVISIBLE);
                m_OTP.setVisibility(View.VISIBLE);
                m_verifyButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initializeVariables() {
        m_send_verCodeButton=findViewById(R.id.send_ver_code_button);
        m_verifyButton=findViewById(R.id.verify_button);
        m_OTP=findViewById(R.id.verification_number_login);
        m_phoneNumber=findViewById(R.id.phone_number_login);
    }
}
