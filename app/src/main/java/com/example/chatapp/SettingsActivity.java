package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView m_updateImage;
    private Button m_updateButton;
    private EditText m_updateUsername,m_updateStatus;
    private FirebaseAuth m_auth;
    private DatabaseReference m_databaseReference;
    private String m_currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initializeVariables();
        m_auth=FirebaseAuth.getInstance();
        m_databaseReference=FirebaseDatabase.getInstance().getReference();
        m_currentUserId=m_auth.getCurrentUser().getUid();
        m_updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatus();
            }
        });
        RetrieveUserInfo();
        m_updateUsername.setVisibility(View.INVISIBLE);
    }

    private void RetrieveUserInfo() {
        m_databaseReference.child("Users").child(m_currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))
                       && (dataSnapshot.hasChild("image")) &&(dataSnapshot.hasChild("status")) )
                       {
                            String username=dataSnapshot.child("name").getValue().toString();
                           String image=dataSnapshot.child("image").getValue().toString();
                           String status=dataSnapshot.child("status").getValue().toString();
                           m_updateUsername.setText(username);
                           m_updateStatus.setText(status);
                       }
                       else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))
                               &&(dataSnapshot.hasChild("status"))){
                           String username=dataSnapshot.child("name").getValue().toString();
                           String status=dataSnapshot.child("status").getValue().toString();
                           m_updateUsername.setText(username);
                           m_updateStatus.setText(status);
                       }
                       else
                       {
                           m_updateUsername.setVisibility(View.VISIBLE);
                           Toast.makeText(SettingsActivity.this, "Please enter data ...", Toast.LENGTH_SHORT).show();
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void updateStatus() {
        String username = m_updateUsername.getText().toString();
        String status = m_updateStatus.getText().toString();
        if (username.isEmpty()) {
            Toast.makeText(this, "Username must nt be null", Toast.LENGTH_SHORT).show();
        }
        if (status.isEmpty()) {
            Toast.makeText(this, "Staus must not empty", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", m_currentUserId);
            profileMap.put("name", username);
            profileMap.put("status", status);
            m_databaseReference.child("Users").child(m_currentUserId).setValue(profileMap)
                    .addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        startMainActivity();
                                        Toast.makeText(SettingsActivity.this, "Account Created Successfully...", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String message = task.getException().toString();
                                        Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } );
        }
    }
    private void initializeVariables() {
        m_updateButton=findViewById(R.id.update_settings);
        m_updateImage=findViewById(R.id.profile_image);
        m_updateUsername=findViewById(R.id.set_username);
        m_updateStatus=findViewById(R.id.set_status);
    }
    private void startMainActivity()
    {
        Intent intent=new Intent(SettingsActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
