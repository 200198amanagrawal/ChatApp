package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapp.AllFragments.ModelClass.GroupDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupDetailsActivity extends AppCompatActivity {

    private Toolbar m_toolBar;
    HashSet<String> m_GroupUserIds;
    CircleImageView m_GroupIcon,m_GroupDetailSubmit;
    private FirebaseAuth mAuth;
    private String m_currentUserID;
    private DatabaseReference m_GroupRef;
    EditText m_GroupName;
    private DatabaseReference m_GroupRefChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        m_toolBar=findViewById(R.id.groupDetailsToolbar);
        setSupportActionBar(m_toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Group Details");
        mAuth = FirebaseAuth.getInstance();
        m_currentUserID = mAuth.getCurrentUser().getUid();
        m_GroupRef= FirebaseDatabase.getInstance().getReference().child("GroupDetails");
        m_GroupRefChat= FirebaseDatabase.getInstance().getReference().child("GroupChat");
        m_GroupUserIds= (HashSet<String>) getIntent().getExtras().get("UserIds");
        m_GroupUserIds.add(m_currentUserID);
        m_GroupName=findViewById(R.id.groupName);
        m_GroupIcon=findViewById(R.id.groupIcon);
        m_GroupDetailSubmit=findViewById(R.id.send_group_details);
        m_GroupDetailSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m_GroupName.getText().toString()==null)
                {
                    Toast.makeText(GroupDetailsActivity.this, "Enter Some Group Name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    createGroup();
                }
            }
        });

    }

    private void createGroup()
    {
        GroupDetails groupDetails=new GroupDetails("",m_GroupName.getText().toString());
        String groupRefID=m_GroupRef.push().getKey();
        m_GroupRef.child(groupRefID).setValue(groupDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(GroupDetailsActivity.this, "Group Details Saved", Toast.LENGTH_SHORT).show();
                }
            }
        });
        for(String userID:m_GroupUserIds)
        {
                    m_GroupRefChat.child(groupRefID).child(userID).setValue("").addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(GroupDetailsActivity.this, "Group Created Successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent=new Intent(GroupDetailsActivity.this,MainActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }
                    );

        }
    }

}