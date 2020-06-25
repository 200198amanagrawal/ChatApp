package com.example.chatapp.AllFragments.GroupChats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar m_toolbar;
    private ImageButton m_sendMsgButton;
    private EditText m_usermessage;
    private ScrollView m_scrollView;
    private TextView m_displayTextMessage;
    private String m_currentGroupName,m_currentUserId,m_CurrentUserName,m_currentDate,m_currentTime;
    private FirebaseAuth m_auth;
    private DatabaseReference m_UserRef,m_GroupRef,m_GroupMsgKeyRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        m_currentGroupName=getIntent().getExtras().get("groupName").toString();
        Toast.makeText(this, m_currentGroupName, Toast.LENGTH_SHORT).show();

        m_auth=FirebaseAuth.getInstance();
        m_currentUserId=m_auth.getCurrentUser().getUid();
        m_UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        m_GroupRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(m_currentGroupName);

        initializeFields();
        getUserInfo();
        m_sendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMsfToDB();
                m_usermessage.setText("");
                m_scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    private void saveMsfToDB() {
        String message=m_usermessage.getText().toString();
        String messgaeKey=m_GroupRef.push().getKey();
        if(TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Write some msg to send...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calendar=Calendar.getInstance();
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MMM dd,yyyy");
            m_currentDate=simpleDateFormat.format(calendar.getTime());

            Calendar calendartime=Calendar.getInstance();
            SimpleDateFormat simpleTimeFormat=new SimpleDateFormat("hh:mm a");
            m_currentTime=simpleTimeFormat.format(calendartime.getTime());

            HashMap<String,Object> groupmsgKey=new HashMap<>();
            m_GroupRef.updateChildren(groupmsgKey);
            m_GroupMsgKeyRef=m_GroupRef.child(messgaeKey);//this will take the key and append the rest of the data
            HashMap<String,Object> msgInfoMap=new HashMap<>();
            msgInfoMap.put("name",m_CurrentUserName);
            msgInfoMap.put("message",message);
            msgInfoMap.put("date",m_currentDate);
            msgInfoMap.put("time",m_currentTime);
            m_GroupMsgKeyRef.updateChildren(msgInfoMap);
        }
    }

    private void getUserInfo() {
        m_UserRef.child(m_currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    m_CurrentUserName=dataSnapshot.child("name").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields() {
        m_toolbar=findViewById(R.id.group_chat_toolbar);
        setSupportActionBar(m_toolbar);
        getSupportActionBar().setTitle(m_currentGroupName);
        m_sendMsgButton =findViewById(R.id.send_message);
        m_usermessage=findViewById(R.id.input_group_message);
        m_displayTextMessage=findViewById(R.id.group_chat_text_display);
        m_scrollView=findViewById(R.id.group_chat_scrollview);
    }

    @Override
    protected void onStart() {
        super.onStart();
        m_GroupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator=dataSnapshot.getChildren().iterator();
        while (iterator.hasNext())
        {
            String chatDate=(String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage=(String) ((DataSnapshot) iterator.next()).getValue();
            String chatName=(String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime=(String) ((DataSnapshot) iterator.next()).getValue();
            m_displayTextMessage.append(chatName+"\n"+chatMessage+"\n"+chatTime+"   "+chatDate+"\n\n\n");
            m_scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
