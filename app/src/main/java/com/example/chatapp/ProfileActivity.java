package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private CircleImageView m_ProfilePhoto;
    private TextView m_profileUser,m_profileStatus;
    private Button m_sendMsgButton,m_declineMsgButton;
    private String recieverUserId,m_currentState, m_senderUserId;
    private DatabaseReference m_UserRef,m_ChatRequestRef,m_ContactsRef, m_NotificationRef;
    private FirebaseAuth m_Auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        recieverUserId=getIntent().getExtras().get("visitUserId").toString();

        m_ProfilePhoto=findViewById(R.id.visit_profile_image);
        m_profileUser=findViewById(R.id.visit_profile_username);
        m_profileStatus=findViewById(R.id.visit_profile_status);
        m_sendMsgButton=findViewById(R.id.send_msg_req_button);
        m_declineMsgButton=findViewById(R.id.decline_msg_request);

        m_UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        m_Auth=FirebaseAuth.getInstance();
        m_senderUserId =m_Auth.getCurrentUser().getUid();
        m_ChatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        m_ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        m_NotificationRef =FirebaseDatabase.getInstance().getReference().child("Notifications");

        m_currentState="new";
        retrieveUserInfo();
    }

    private void retrieveUserInfo() {
        m_UserRef.child(recieverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( (dataSnapshot.exists()) && (dataSnapshot.hasChild("image")) )
                {
                    String username=dataSnapshot.child("name").getValue().toString();
                    String userstatus=dataSnapshot.child("status").getValue().toString();
                    String userimage=dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(userimage).placeholder(R.drawable.profile_image).into(m_ProfilePhoto);
                    m_profileUser.setText(username);
                    m_profileStatus.setText(userstatus);
                    ManageChatRequests();
                }
                else
                {
                    String username=dataSnapshot.child("name").getValue().toString();
                    String userstatus=dataSnapshot.child("status").getValue().toString();
                    m_profileUser.setText(username);
                    m_profileStatus.setText(userstatus);
                    ManageChatRequests();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequests() {

        m_ChatRequestRef.child(m_senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(recieverUserId))
                {
                    String request_type=dataSnapshot.child(recieverUserId).child("request_type").getValue().toString();
                    if(request_type.equals("sent"))
                    {
                        m_currentState="request_sent";
                        m_sendMsgButton.setText("Cancel Chat Request");
                    }
                    else if(request_type.equals("received"))
                    {
                        m_currentState="request_received";
                        m_sendMsgButton.setText("Accept Chat Request");
                        m_declineMsgButton.setVisibility(View.VISIBLE);
                        m_declineMsgButton.setEnabled(true);
                        m_declineMsgButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest();
                            }
                        });
                    }
                }
                else
                {
                    m_ContactsRef.child(m_senderUserId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(recieverUserId))
                                    {
                                        m_currentState="friends";
                                        m_sendMsgButton.setText("Unfriend");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!m_senderUserId.equals(recieverUserId))
        {
            m_sendMsgButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_sendMsgButton.setEnabled(false);
                    if(m_currentState.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if(m_currentState.equals("request_sent"))
                    {
                        cancelChatRequest();
                    }
                    if(m_currentState.equals("request_received"))
                    {
                        acceptChatRequest();
                    }
                    if(m_currentState.equals("friends"))
                    {
                        removeContact();
                    }
                }
            });
        }
        else {
            m_sendMsgButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeContact() {
        m_ContactsRef.child(m_senderUserId).child(recieverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    m_ContactsRef.child(recieverUserId).child(m_senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                m_sendMsgButton.setEnabled(true);
                                m_currentState="new";
                                m_sendMsgButton.setText("Send Message");

                                m_declineMsgButton.setVisibility(View.INVISIBLE);
                                m_declineMsgButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptChatRequest() {

        m_ContactsRef.child(m_senderUserId).child(recieverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            m_ContactsRef.child(recieverUserId).child(m_senderUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                m_ChatRequestRef.child(m_senderUserId).child(recieverUserId)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            m_ChatRequestRef.child(recieverUserId).child(m_senderUserId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                        m_sendMsgButton.setEnabled(true);
                                                                        m_currentState="friends";
                                                                        m_sendMsgButton.setText("Unfriend");
                                                                        m_declineMsgButton.setVisibility(View.INVISIBLE);
                                                                        m_declineMsgButton.setEnabled(false);
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelChatRequest() {
        m_ChatRequestRef.child(m_senderUserId).child(recieverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    m_ChatRequestRef.child(recieverUserId).child(m_senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                m_sendMsgButton.setEnabled(true);
                                m_currentState="new";
                                m_sendMsgButton.setText("Send Message");

                                m_declineMsgButton.setVisibility(View.INVISIBLE);
                                m_declineMsgButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest() {
        m_ChatRequestRef.child(m_senderUserId).child(recieverUserId).child("request_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    m_ChatRequestRef.child(recieverUserId).child(m_senderUserId).child("request_type")
                            .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                HashMap<String,String> chatNotification=new HashMap<>();
                                chatNotification.put("from",m_senderUserId);
                                chatNotification.put("type","request");
                                m_NotificationRef.child(recieverUserId).push().setValue(chatNotification)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    m_sendMsgButton.setEnabled(true);
                                                    m_currentState="request_sent";
                                                    m_sendMsgButton.setText("Cancel Chat request");
                                                }
                                            }
                                        });

                            }
                        }
                    });
                }
            }
        });
    }
}
