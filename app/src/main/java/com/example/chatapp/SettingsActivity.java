package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView m_updateImage;
    private Button m_updateButton;
    private EditText m_updateUsername,m_updateStatus;
    private FirebaseAuth m_auth;
    private DatabaseReference m_databaseReference;
    private String m_currentUserId;
    private int m_GalleryPick=1;
    private StorageReference m_UserProfileImageReferences;
    private ProgressDialog loadingBar;
    private Toolbar m_Toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initializeVariables();
        m_updateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleyIntent=new Intent();
                galleyIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleyIntent.setType("image/*");
                startActivityForResult(galleyIntent,m_GalleryPick);
            }
        });
        m_auth=FirebaseAuth.getInstance();
        m_databaseReference=FirebaseDatabase.getInstance().getReference();
        m_currentUserId=m_auth.getCurrentUser().getUid();
        m_UserProfileImageReferences= FirebaseStorage.getInstance().getReference().child("Profile Images");
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
                           Picasso.get().load(image).into(m_updateImage);
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
            Toast.makeText(this, "Status must not empty", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", m_currentUserId);
            profileMap.put("name", username);
            profileMap.put("status", status);
            m_databaseReference.child("Users").child(m_currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        startMainActivity();
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
        loadingBar=new ProgressDialog(this);
        m_Toolbar=findViewById(R.id.settings_toolbar);
        setSupportActionBar(m_Toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==m_GalleryPick && resultCode==RESULT_OK && data!=null)
        {
            Uri imageUri=data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK)
            {
                loadingBar.setTitle("Setting Profile Image");
                loadingBar.setMessage("Please wait while we are updating image");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                Uri resultUri=result.getUri();
                StorageReference filePath=m_UserProfileImageReferences.child(m_currentUserId+".jpg");
                filePath.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadUrl = uri.toString();

                                        m_databaseReference.child("Users").child(m_currentUserId).child("image")
                                                .setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(SettingsActivity.this, "Image saved in database successfuly", Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }
                                                        else{
                                                            String message = task.getException().toString();
                                                            Toast.makeText(SettingsActivity.this, "Error: " + message,Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();

                                                        }

                                                    }
                                                });
                                    }
                                });

                            }
                        });

            }
        }
    }

    private void startMainActivity()
    {
        Intent intent=new Intent(SettingsActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
