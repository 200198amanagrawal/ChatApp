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
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private TextView m_newaccount;
    private FirebaseAuth m_auth;
    private EditText m_loginEmail,m_loginPassword;
    private Button m_loginButton,m_phoneLoginButton;
    private ProgressDialog progressDialog;
    private DatabaseReference m_UsersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_auth=FirebaseAuth.getInstance();
        m_UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        setContentView(R.layout.activity_login);
        initializeVariables();
        m_phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneLoginIntent=new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });
        startRegisterActivity();
        startLoginActivity();
    }

    private void startRegisterActivity() {
        m_newaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startLoginActivity() {
        m_loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAccount();
            }
        });
    }
    private void loginAccount() {
        String email=m_loginEmail.getText().toString();
        String password=m_loginPassword.getText().toString();
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(LoginActivity.this, "Please enter email...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(LoginActivity.this, "Please enter password...", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.setTitle("Signing in to your Account");
            progressDialog.setMessage("Please wait,while we are creating new account");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            m_auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                String currentUserId=m_auth.getCurrentUser().getUid();
                                String deviceToken= FirebaseInstanceId.getInstance().getToken();
                                m_UsersRef.child(currentUserId).child("device_token")
                                        .setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    startMainActivity();
                                                    Toast.makeText(LoginActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });

                            }
                            else {
                                String message=task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    }
            );
        }
    }
    private void initializeVariables() {
        m_loginEmail=findViewById(R.id.login_email);
        m_loginPassword=findViewById(R.id.login_password);
        m_loginButton=findViewById(R.id.login_button);
        m_phoneLoginButton=findViewById(R.id.phone_button);
        m_newaccount=findViewById(R.id.new_account);
        progressDialog=new ProgressDialog(this);
    }

    private void startMainActivity()
    {
        Intent intent=new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
