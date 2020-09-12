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

public class RegisterActivity extends AppCompatActivity {

    private TextView m_loginactivity;
    private EditText m_regmail,m_regpass;
    private Button m_register;
    private FirebaseAuth m_auth;
    private DatabaseReference m_RootRef;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initialize();
        startLoginActivity();
        m_auth=FirebaseAuth.getInstance();
        m_RootRef = FirebaseDatabase.getInstance().getReference();
        m_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    private void createNewAccount() {
        String email=m_regmail.getText().toString();
        String password=m_regpass.getText().toString();
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(RegisterActivity.this, "Please enter email...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(RegisterActivity.this, "Please enter password...", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.setTitle("Creating New Account");
            progressDialog.setMessage("Please wait,while we are creating new account");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            m_auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                String currentUserId=m_auth.getUid();
                                String deviceToken= FirebaseInstanceId.getInstance().getToken();
                                m_RootRef.child("Users").child(currentUserId).setValue("");
                                m_RootRef.child("Users").child(currentUserId).child("device_token")
                                        .setValue(deviceToken);
                                startMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account Created Successfully...", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                            else {
                                String message=task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    }
            );
        }
    }

    private void startMainActivity() {
        Intent intent=new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void initialize() {
        m_regmail=findViewById(R.id.register_email);
        m_regpass=findViewById(R.id.register_password);
        m_register=findViewById(R.id.register_button);
        progressDialog=new ProgressDialog(this);
        m_loginactivity=findViewById(R.id.existing_account);
    }

    private void startLoginActivity() {
        m_loginactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(intent);
    }
}
