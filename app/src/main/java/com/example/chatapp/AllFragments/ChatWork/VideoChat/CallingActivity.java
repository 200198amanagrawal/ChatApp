package com.example.chatapp.AllFragments.ChatWork.VideoChat;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
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

public class CallingActivity extends AppCompatActivity {

    private TextView m_receiverUserName;
    private ImageView m_receiverImage;
    private String receiverID;
    private String receiverImage;
    private String receiverName;
    private String senderID, callingID = "", ringingID = "";
    private DatabaseReference m_usersRef;
    private CircleImageView m_endCall, m_acceptCall;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);
        m_receiverUserName = findViewById(R.id.receiver_calling_userName);
        m_receiverImage = findViewById(R.id.receiver_calling_image);
        senderID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Intent intent = getIntent();
        receiverID = intent.getExtras().get("receiverID").toString();
        m_usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        m_endCall = findViewById(R.id.receiver_end_call);
        m_acceptCall = findViewById(R.id.receiver_receive_call);
        getAndSetUserInfo();
        mediaPlayer=MediaPlayer.create(this,R.raw.ringing);

        m_endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                cancelCallingUser();
            }
        });

        m_acceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                final HashMap<String,Object> callingPickupMap=new HashMap<>();
                callingPickupMap.put("picked","picked");
                m_usersRef.child(senderID).child("Ringing")
                        .updateChildren(callingPickupMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isComplete())
                                {
                                    Intent intent1=new Intent(CallingActivity.this, VideoChatActivity.class);
                                    intent1.putExtra("senderID",senderID);
                                    intent1.putExtra("receiverID",receiverID);
                                    startActivity(intent1);
                                }
                            }
                        });
            }
        });
    }


    private void getAndSetUserInfo() {
        m_usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(receiverID).exists()) {
                    if (snapshot.child(receiverID).hasChild("image")) {
                        receiverImage = snapshot.child(receiverID).child("image").getValue().toString();
                        Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(m_receiverImage);
                    }
                    receiverName = snapshot.child(receiverID).child("name").getValue().toString();
                    m_receiverUserName.setText(receiverName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer.start();
        m_usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(senderID).hasChild("Ringing") &&
                        !(snapshot.child(senderID).hasChild("Calling"))) {
                    m_acceptCall.setVisibility(View.VISIBLE);
                }

                if(snapshot.child(receiverID).child("Ringing").hasChild("picked"))
                {
                    mediaPlayer.stop();
                    Intent intent1=new Intent(CallingActivity.this, VideoChatActivity.class);
                    intent1.putExtra("senderID",senderID);
                    intent1.putExtra("receiverID",receiverID);
                    startActivity(intent1);
                }
                if(snapshot.child(receiverID).child("Dropped").hasChild("dropped"))
                {
                    m_usersRef.child(receiverID).child("Dropped").removeValue();
                    mediaPlayer.stop();
                    startActivity(new Intent(CallingActivity.this,MainActivity.class));
                }
                if(snapshot.child(senderID).child("Dropped").hasChild("dropped"))
                {
                    m_usersRef.child(senderID).child("Dropped").removeValue();
                    mediaPlayer.stop();
                    startActivity(new Intent(CallingActivity.this,MainActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void cancelCallingUser() {

        m_usersRef.child(senderID)
                .child("Calling")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild("calling")) {
                            callingID = snapshot.child("calling").getValue().toString();
                            m_usersRef.child(callingID)
                                    .child("Ringing")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                m_usersRef.child(senderID)
                                                        .child("Calling")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                final HashMap<String,Object> callingPickupMap=new HashMap<>();
                                                                callingPickupMap.put("dropped","dropped");
                                                                m_usersRef.child(receiverID).child("Dropped")
                                                                        .updateChildren(callingPickupMap);
                                                                startActivity(new Intent(CallingActivity.this,
                                                                        MainActivity.class));
                                                                finish();
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        } else {
                            startActivity(new Intent(CallingActivity.this,
                                    MainActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //from receiver's side

        m_usersRef.child(senderID)
                .child("Ringing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild("ringing")) {
                            ringingID = snapshot.child("ringing").getValue().toString();
                            m_usersRef.child(ringingID)
                                    .child("Calling")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                m_usersRef.child(senderID)
                                                        .child("Ringing")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                final HashMap<String,Object> callingPickupMap=new HashMap<>();
                                                                callingPickupMap.put("dropped","dropped");
                                                                m_usersRef.child(senderID).child("Dropped")
                                                                        .updateChildren(callingPickupMap);
                                                                startActivity(new Intent(CallingActivity.this,
                                                                        MainActivity.class));
                                                                finish();
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        } else {
                            startActivity(new Intent(CallingActivity.this,
                                    MainActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}
