package com.example.chatapp.AllFragments.GroupChats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.AllFragments.ModelClass.Messages;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    private String m_msgSenderID;
    private ArrayList<String> m_msgReceiverIDs;
    private String m_GroupName;
    private TextView m_UserGroupName,m_Lastseen;
    private CircleImageView m_UserImage;
    private Toolbar m_chatToolbar;
    private ImageButton m_sendMsgButton;
    private EditText m_msgInputText;
    private FirebaseAuth mAuth;
    private DatabaseReference m_RootRef,m_gorupRef;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupMessagesAdapter messagesAdapter;
    private RecyclerView m_UsersList;
    private String saveCurrentTime, saveCurrentDate;

    private ImageButton  m_SendFilesButton;
    private String checker="",myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;
    private ChildEventListener childEventListener;
    private String m_groupID;
    private DatabaseReference userMsgRef;

    public boolean isSentFlag() {
        return sentFlag;
    }

    public void setSentFlag(boolean sentFlag) {
        this.sentFlag = sentFlag;
    }

    boolean sentFlag=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        mAuth=FirebaseAuth.getInstance();
        m_msgSenderID=mAuth.getCurrentUser().getUid();
        m_RootRef= FirebaseDatabase.getInstance().getReference();

        Map<String,Object> userIDData= (HashMap<String,Object>) getIntent().getExtras().get("userIDs");
        m_msgReceiverIDs=  new ArrayList<>();
        for(String userIDs:userIDData.keySet())
        {
            m_msgReceiverIDs.add(userIDs);
        }
        m_msgReceiverIDs.remove(m_msgSenderID);
        m_GroupName=getIntent().getExtras().get("groupName").toString();
        m_groupID=getIntent().getExtras().get("groupID").toString();

        m_gorupRef=FirebaseDatabase.getInstance().getReference().child("GroupMessages").child(m_groupID);

        initializeControllers();

        m_UserGroupName.setText(m_GroupName);
        m_Lastseen.setText("");
        m_sendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg();
            }
        });

        childEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages=dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messagesAdapter.notifyDataSetChanged();
                m_UsersList.smoothScrollToPosition(m_UsersList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
        };
    }


    private void initializeControllers() {
        m_chatToolbar=findViewById(R.id.group_chat_toolbar);
        setSupportActionBar(m_chatToolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView=layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        m_UserImage=findViewById(R.id.custom_profile_image);
        m_Lastseen=findViewById(R.id.custom_last_seen);
        m_UserGroupName =findViewById(R.id.custom_profile_name);

        m_sendMsgButton=findViewById(R.id.group_SENDMSG_button);
        m_msgInputText =findViewById(R.id.group_input_message);

        messagesAdapter=new GroupMessagesAdapter(messagesList);
        m_UsersList=findViewById(R.id.group_private_message_list);
        linearLayoutManager=new LinearLayoutManager(this);

        m_UsersList.setLayoutManager(linearLayoutManager);
        m_UsersList.setAdapter(messagesAdapter);

        m_SendFilesButton = findViewById(R.id.group_send_FORI_btn);
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
        loadingBar=new ProgressDialog(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        messagesList.clear();
        m_gorupRef.child(m_msgSenderID).
                removeEventListener(childEventListener);}

    @Override
    protected void onPause() {
        super.onPause();
        messagesList.clear();
            m_gorupRef.child(m_msgSenderID).
                removeEventListener(childEventListener);}

    @Override
    protected void onStart() {
        super.onStart();

//        if(isSentFlag())
//        {
//            for(String m_msgReceiverID:m_msgReceiverIDs){
//                m_RootRef.child("GroupMessages").child(m_groupID).child(m_msgSenderID).child(m_msgReceiverID)
//                        .addChildEventListener(childEventListener);break;}
//            setSentFlag(false);
//        }
        m_gorupRef.child(m_msgSenderID).addChildEventListener(childEventListener);

    }
    private void sendMsg() {

        final String messageText=m_msgInputText.getText().toString();
        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Write some msg to send ..", Toast.LENGTH_SHORT).show();
        }
        else {
            m_gorupRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    String userID= snapshot.getKey();
                    userMsgRef = m_RootRef.child("GroupMessages").child(m_groupID).
                            child(m_msgSenderID).push();
                    String msgPushID=userMsgRef.getKey();
                    Map msgTextBody=new HashMap();
                    msgTextBody.put("message",messageText);
                    msgTextBody.put("type","text");
                    msgTextBody.put("from",m_msgSenderID);
                    if(userID.equals(m_msgSenderID))
                    {
                        msgTextBody.put("sentOrReceived", "sent");
                    }
                    else {
                        msgTextBody.put("sentOrReceived", "received");
                    }
                    msgTextBody.put("to",userID);
                    msgTextBody.put("messageID", msgPushID);
                    msgTextBody.put("time", saveCurrentTime);
                    msgTextBody.put("date", saveCurrentDate);
                    msgTextBody.put("groupID",m_groupID);
                    m_gorupRef.child(userID).child(msgPushID).updateChildren(msgTextBody).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (!task.isSuccessful())
                            {
                                Toast.makeText(GroupChatActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                            m_msgInputText.setText("");
                        }
                    });
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
