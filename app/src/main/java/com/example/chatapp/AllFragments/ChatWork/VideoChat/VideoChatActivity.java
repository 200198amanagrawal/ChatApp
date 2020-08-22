package com.example.chatapp.AllFragments.ChatWork.VideoChat;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

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
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_KEY = "46893654", SESSION_ID = "1_MX40Njg5MzY1NH5-MTU5ODEwMjI5ODU5Nn4zY1dyWk9HMzdUVlI1bHFGdlZMSXlkcWh-fg",
            TOKEN = "T1==cGFydG5lcl9pZD00Njg5MzY1NCZzaWc9MmFmNjk2OGI4NGE1ZTMyM2Q1ZDI2NmQ2ZDFmMmE1MWY0OGUwNTAxYTpzZXNzaW9uX2lkPTFfTVg0ME5qZzVNelkxTkg1LU1UVTVPREV3TWpJNU9EVTVObjR6WTFkeVdrOUhNemRVVmxJMWJIRkdkbFpNU1hsa2NXaC1mZyZjcmVhdGVfdGltZT0xNTk4MTAyNDAwJm5vbmNlPTAuNjE1OTA5ODg5MTc4NzIyOSZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNjAwNjk0NDAyJmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;
    private CircleImageView m_closeVideoChatBtn;
    private DatabaseReference m_usersRef;
    private String m_userID = "", m_SenderUserID = "", m_ReceiverUserID = "";
    private FrameLayout m_PublisherViewController;
    private FrameLayout m_SubscriberViewController;
    private Session m_Session;
    private Publisher m_Publisher;
    private Subscriber m_Subsriber;
    private String senderID, callingID = "", ringingID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);
        m_usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        m_userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        m_SenderUserID = getIntent().getExtras().get("senderID").toString();
        m_ReceiverUserID = getIntent().getExtras().get("receiverID").toString();

        m_closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);
        m_closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCallingUser();
            }
        });
        requestPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();

        m_usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(m_ReceiverUserID).child("Dropped").hasChild("dropped")) {
                    m_usersRef.child(m_ReceiverUserID).child("Dropped").removeValue();
                    startActivity(new Intent(VideoChatActivity.this, MainActivity.class));
                }
                if (snapshot.child(m_userID).child("Dropped").hasChild("dropped")) {
                    m_usersRef.child(m_userID).child("Dropped").removeValue();
                    startActivity(new Intent(VideoChatActivity.this, MainActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChatActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            m_PublisherViewController = findViewById(R.id.publisher_container);
            m_SubscriberViewController = findViewById(R.id.subsciber_container);
            m_Session = new Session.Builder(this, API_KEY, SESSION_ID).build();
            m_Session.setSessionListener(VideoChatActivity.this);
            m_Session.connect(TOKEN);
        } else {
            EasyPermissions.requestPermissions(this, "Hey this app requies mic and Camera permissions", RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");
        m_Publisher = new Publisher.Builder(this).build();
        m_Publisher.setPublisherListener(VideoChatActivity.this);
        m_PublisherViewController.addView(m_Publisher.getView());
        if (m_Publisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) m_Publisher.getView()).setZOrderOnTop(true);
        }
        m_Session.publish(m_Publisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Stream Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");
        if (m_Subsriber == null) {
            m_Subsriber = new Subscriber.Builder(this, stream).build();
            m_Session.subscribe(m_Subsriber);
            m_SubscriberViewController.addView(m_Subsriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");
        if (m_Subsriber != null) {
            m_Subsriber = null;
            m_SubscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "Stream Error");
    }

    private void cancelCallingUser() {

        m_usersRef.child(m_userID)
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
                                                m_usersRef.child(m_userID)
                                                        .child("Calling")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (m_Publisher != null) {
                                                                    m_Publisher.destroy();
                                                                }
                                                                if (m_Subsriber != null) {
                                                                    m_Subsriber.destroy();
                                                                }
                                                                final HashMap<String, Object> callingPickupMap = new HashMap<>();
                                                                callingPickupMap.put("dropped", "dropped");
                                                                m_usersRef.child(m_ReceiverUserID).child("Dropped")
                                                                        .updateChildren(callingPickupMap);
                                                                startActivity(new Intent(VideoChatActivity.this,
                                                                        MainActivity.class));
                                                                finish();
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        } else {
                            startActivity(new Intent(VideoChatActivity.this,
                                    MainActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //from receiver's side

        m_usersRef.child(m_userID)
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
                                                m_usersRef.child(m_userID)
                                                        .child("Ringing")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (m_Publisher != null) {
                                                                    m_Publisher.destroy();
                                                                }
                                                                if (m_Subsriber != null) {
                                                                    m_Subsriber.destroy();
                                                                }
                                                                final HashMap<String, Object> callingPickupMap = new HashMap<>();
                                                                callingPickupMap.put("dropped", "dropped");
                                                                m_usersRef.child(m_userID).child("Dropped")
                                                                        .updateChildren(callingPickupMap);
                                                                startActivity(new Intent(VideoChatActivity.this,
                                                                        MainActivity.class));
                                                                finish();
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        } else {
                            startActivity(new Intent(VideoChatActivity.this,
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
