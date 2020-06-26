package com.example.chatapp.SignupAndLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button m_send_verCodeButton,m_verifyButton;
    private EditText m_OTP,m_phoneNumber;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks m_Callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private CountryCodePicker ccp;
    private String selected_country_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        mAuth=FirebaseAuth.getInstance();
        initializeVariables();
        ccp = findViewById(R.id.ccp);
        ccp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
                    @Override
                    public void onCountrySelected() {
                        selected_country_code = ccp.getSelectedCountryCodeWithPlus();
                    }
                });
            }
        });
        m_send_verCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber=selected_country_code+m_phoneNumber.getText().toString();
                if(TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Enter phone no...", Toast.LENGTH_SHORT).show();
                }
                else
                    {
                        m_send_verCodeButton.setVisibility(View.INVISIBLE);
                        m_phoneNumber.setVisibility(View.INVISIBLE);
                        m_OTP.setVisibility(View.VISIBLE);
                        m_verifyButton.setVisibility(View.VISIBLE);
                        loadingBar.setTitle("Phone Verification");
                        loadingBar.setMessage("Please wait while we are authenticating your phone");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber,        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        PhoneLoginActivity.this, // Activity (for callback binding)
                        m_Callbacks);        // OnVerificationStateChangedCallbacks

                }
            }
        });
        m_verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_send_verCodeButton.setVisibility(View.INVISIBLE);
                m_phoneNumber.setVisibility(View.INVISIBLE);
                String m_verificationCode=m_OTP.getText().toString();
                if(TextUtils.isEmpty(m_verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Cannot be empty..", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("OTP Verification");
                    loadingBar.setMessage("Please wait while we are authenticating your OTP");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, m_verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
        m_Callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                m_send_verCodeButton.setVisibility(View.VISIBLE);
                m_phoneNumber.setVisibility(View.VISIBLE);
                m_OTP.setVisibility(View.INVISIBLE);
                m_verifyButton.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                m_send_verCodeButton.setVisibility(View.INVISIBLE);
                m_phoneNumber.setVisibility(View.VISIBLE);
                m_OTP.setVisibility(View.VISIBLE);
                m_verifyButton.setVisibility(View.VISIBLE);
            }
        };
    }

    private void initializeVariables() {
        m_send_verCodeButton=findViewById(R.id.send_ver_code_button);
        m_verifyButton=findViewById(R.id.verify_button);
        m_OTP=findViewById(R.id.verification_number_login);
        m_phoneNumber=findViewById(R.id.phone_number_login);
        loadingBar=new ProgressDialog(this);
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congrats ..", Toast.LENGTH_SHORT).show();
                            sendUsertoMainActivity();
                        } else {
                            String exception=task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, exception, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUsertoMainActivity() {
        Intent intent=new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
