package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String m_msgReceiverID,m_msgReceiverName,m_msgReceiverImage,m_msgSenderID;
    private TextView m_Username,m_Lastseen;
    private CircleImageView m_UserImage;
    private Toolbar m_chatToolbar;
    private ImageButton m_sendMsgButton;
    private EditText m_msgInputText;
    private FirebaseAuth mAuth;
    private DatabaseReference m_RootRef;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView m_UsersList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        m_msgSenderID=mAuth.getCurrentUser().getUid();
        m_RootRef= FirebaseDatabase.getInstance().getReference();

        m_msgReceiverID=getIntent().getExtras().get("visit_user_id").toString();
        m_msgReceiverName=getIntent().getExtras().get("visit_user_name").toString();
        m_msgReceiverImage=getIntent().getExtras().get("visit_user_image").toString();


        initializeControllers();

        m_Username.setText(m_msgReceiverName);
        Picasso.get().load(m_msgReceiverImage).placeholder(R.drawable.profile_image).into(m_UserImage);

        m_sendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg();
            }
        });
    }


    private void initializeControllers() {

        m_chatToolbar=findViewById(R.id.chat_toolbar);
        setSupportActionBar(m_chatToolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView=layoutInflater.inflate(R.layout.custom_char_bar,null);
        actionBar.setCustomView(actionBarView);

        m_UserImage=findViewById(R.id.custom_profile_image);
        m_Lastseen=findViewById(R.id.custom_last_seen);
        m_Username=findViewById(R.id.custom_profile_name);

        m_sendMsgButton=findViewById(R.id.send_msg_button);
        m_msgInputText =findViewById(R.id.input_message);

        messagesAdapter=new MessagesAdapter(messagesList);
        m_UsersList=findViewById(R.id.private_message_list);
        linearLayoutManager=new LinearLayoutManager(this);

        m_UsersList.setLayoutManager(linearLayoutManager);
        m_UsersList.setAdapter(messagesAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        m_RootRef.child("Messages").child(m_msgSenderID).child(m_msgReceiverID)
                .addChildEventListener(new ChildEventListener() {
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
                });
    }

    private void sendMsg() {

        String messageText=m_msgInputText.getText().toString();
        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Write some msg to send ..", Toast.LENGTH_SHORT).show();
        }
        else{
            String msgSenderRef="Messages/"+m_msgSenderID+"/"+m_msgReceiverID;
            String msgReceiverRef="Messages/"+m_msgReceiverID+"/"+m_msgSenderID;
            DatabaseReference userMsgRef=m_RootRef.child("Messages").child(m_msgSenderID).child(m_msgReceiverID)
                    .push();
            String msgPushID=userMsgRef.getKey();//this will generate a randrom new key everytime.

            Map msgTextBody=new HashMap();
            msgTextBody.put("message",messageText);
            msgTextBody.put("type","text");
            msgTextBody.put("from",m_msgSenderID);

            Map msgBodyDetails=new HashMap();
            msgBodyDetails.put(msgSenderRef+"/"+msgPushID,msgTextBody);
            msgBodyDetails.put(msgReceiverRef+"/"+msgPushID,msgTextBody);
            m_RootRef.updateChildren(msgBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Msg sent", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    m_msgInputText.setText("");
                }
            });
        }
    }
}
