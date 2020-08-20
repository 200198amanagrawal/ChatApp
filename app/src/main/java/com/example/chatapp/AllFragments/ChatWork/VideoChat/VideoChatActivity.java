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

import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_KEY="46854334",SESSION_ID="1_MX40Njg1NDMzNH5-MTU5NTQ0OTk5NTIzOH5pemd0aFdTVCtDcWtLMWtwN1hNZW84TU1-fg"
            ,TOKEN="T1==cGFydG5lcl9pZD00Njg1NDMzNCZzaWc9OTE4Yzc5YmRlMWYwMWM0MGNhMTBkYTVlNmRkYTc5NzM2ZWRiZGI1MjpzZXNzaW9uX2lkPTFfTVg0ME5qZzFORE16Tkg1LU1UVTVOVFEwT1RrNU5USXpPSDVwZW1kMGFGZFRWQ3REY1d0TE1XdHdOMWhOWlc4NFRVMS1mZyZjcmVhdGVfdGltZT0xNTk1NDUwMDU0Jm5vbmNlPTAuMjk3NjcyOTk4MTYwMTYyMjQmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU5ODA0MjA1NCZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG=VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM=124;
    private CircleImageView m_closeVideoChatBtn;
    private DatabaseReference m_usersRef;
    private String m_userID="",m_SenderUserID="",m_ReceiverUserID="";
    private FrameLayout m_PublisherViewController;
    private FrameLayout m_SubscriberViewController;
    private Session m_Session;
    private Publisher m_Publisher;
    private Subscriber m_Subsriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);
        m_usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        m_userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        m_SenderUserID=getIntent().getExtras().get("senderID").toString();
        m_ReceiverUserID=getIntent().getExtras().get("receiverID").toString();

        m_closeVideoChatBtn=findViewById(R.id.close_video_chat_btn);
        m_closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(m_userID).hasChild("Ringing")) {
                            m_usersRef.child(m_userID).child("Ringing").removeValue();
                            if(m_Publisher!=null)
                            {
                                m_Publisher.destroy();
                            }
                            if(m_Subsriber!=null)
                            {
                                m_Subsriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this, MainActivity.class));
                            if(m_userID.equals(m_SenderUserID))
                            {
                                m_usersRef.child(m_ReceiverUserID).child("Calling").removeValue();
                                startActivity(new Intent(VideoChatActivity.this, MainActivity.class));
                            }
                            else {
                                m_usersRef.child(m_SenderUserID).child("Calling").removeValue();
                                startActivity(new Intent(VideoChatActivity.this, MainActivity.class));
                            }
                        }
                        if(snapshot.child(m_userID).hasChild("Calling")) {
                            m_usersRef.child(m_userID).child("Calling").removeValue();
                            if(m_Publisher!=null)
                            {
                                m_Publisher.destroy();
                            }
                            if(m_Subsriber!=null)
                            {
                                m_Subsriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this, MainActivity.class));
                            if(m_userID.equals(m_SenderUserID))
                            {
                                m_usersRef.child(m_ReceiverUserID).child("Ringing").removeValue();
                                startActivity(new Intent(VideoChatActivity.this, MainActivity.class));
                            }
                            else {
                                m_usersRef.child(m_SenderUserID).child("Ringing").removeValue();
                                startActivity(new Intent(VideoChatActivity.this, MainActivity.class));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoChatActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions()
    {
        String[] perms={Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        if(EasyPermissions.hasPermissions(this,perms))
        {
            m_PublisherViewController=findViewById(R.id.publisher_container);
            m_SubscriberViewController=findViewById(R.id.subsciber_container);
            m_Session=new Session.Builder(this,API_KEY,SESSION_ID).build();
            m_Session.setSessionListener(VideoChatActivity.this);
            m_Session.connect(TOKEN);
        }
        else {
            EasyPermissions.requestPermissions(this,"Hey this app requies mic and Camera permissions",RC_VIDEO_APP_PERM,perms);
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
        m_Publisher=new Publisher.Builder(this).build();
        m_Publisher.setPublisherListener(VideoChatActivity.this);
        m_PublisherViewController.addView(m_Publisher.getView());
        if(m_Publisher.getView() instanceof GLSurfaceView)
        {
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
            if(m_Subsriber==null)
            {
                m_Subsriber=new Subscriber.Builder(this,stream).build();
                m_Session.subscribe(m_Subsriber);
                m_SubscriberViewController.addView(m_Subsriber.getView());
            }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");
        if(m_Subsriber!=null)
        {
            m_Subsriber=null;
            m_SubscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "Stream Error");
    }
}
