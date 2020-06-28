package com.example.chatapp.MenuActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowSettings extends AppCompatActivity {

    private LinearLayout m_ProfileLinearLayout;
    private CircleImageView m_ProfileImage;
    private TextView m_ProfileName,m_ProfileStatus;
    private FirebaseAuth m_auth;
    private DatabaseReference m_databaseReference;
    private String m_currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_settings);
        initializeVariables();
        m_auth=FirebaseAuth.getInstance();
        m_currentUserId=m_auth.getCurrentUser().getUid();
        m_databaseReference= FirebaseDatabase.getInstance().getReference();
        setUserNameStatusImage();
        m_ProfileLinearLayout=findViewById(R.id.profile_info_linear_layout);
        m_ProfileLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ShowSettings.this,UpdateProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setUserNameStatusImage() {
        m_databaseReference.child("Users").child(m_currentUserId).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists())
                                && (dataSnapshot.hasChild("image")) )
                        {
                            String image=dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(image).placeholder(R.drawable.profile_image).into(m_ProfileImage);
                        }
                            String username=dataSnapshot.child("name").getValue().toString();
                            String status=dataSnapshot.child("status").getValue().toString();
                            m_ProfileName.setText(username);
                            m_ProfileStatus.setText(status);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void initializeVariables() {
        m_ProfileImage=findViewById(R.id.profile_image);
        m_ProfileName=findViewById(R.id.profile_username);
        m_ProfileStatus=findViewById(R.id.profile_status);
    }
}
