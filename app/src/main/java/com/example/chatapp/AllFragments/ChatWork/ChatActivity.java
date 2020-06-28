package com.example.chatapp.AllFragments.ChatWork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private String saveCurrentTime, saveCurrentDate;

    private ImageButton  m_SendFilesButton;
    private String checker="",myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;
    private ChildEventListener childEventListener;

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
        displayLastSeen();

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

        m_SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[]=new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Ms Word Files"
                        };
                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the file");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0)
                        {
                            checker = "Images";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent,"Select Image"),438);

                        }
                        else if(which==1)
                        {
                            checker = "pdf";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(Intent.createChooser(intent,"Select PDF"),438);
                        }
                        else if(which==2)
                        {
                            checker = "docx";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(Intent.createChooser(intent,"Select DOCS"),438);
                        }

                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait while we are sending the File");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            fileUri=data.getData();
            if(checker!="Images")
            {
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Document Files");
                final String msgSenderRef="Messages/"+m_msgSenderID+"/"+m_msgReceiverID;
                final String msgReceiverRef="Messages/"+m_msgReceiverID+"/"+m_msgSenderID;
                DatabaseReference userMsgRef=m_RootRef.child("Messages").child(m_msgSenderID).child(m_msgReceiverID)
                        .push();
                final String msgPushID=userMsgRef.getKey();//this will generate a randrom new key everytime.
                final StorageReference filePath=storageReference.child(msgPushID+"."+checker);
                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                Map msgDocumentBody=new HashMap();
                                msgDocumentBody.put("message",uri.toString());
                                msgDocumentBody.put("name",fileUri.getLastPathSegment());
                                msgDocumentBody.put("type",checker);
                                msgDocumentBody.put("from",m_msgSenderID);
                                msgDocumentBody.put("to", m_msgReceiverID);
                                msgDocumentBody.put("messageID", msgPushID);
                                msgDocumentBody.put("time", saveCurrentTime);
                                msgDocumentBody.put("date", saveCurrentDate);

                                Map msgBodyDetails=new HashMap();
                                msgBodyDetails.put(msgSenderRef+"/"+msgPushID,msgDocumentBody);
                                msgBodyDetails.put(msgReceiverRef+"/"+msgPushID,msgDocumentBody);
                                m_RootRef.updateChildren(msgBodyDetails);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double p=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p +"% loading....");
                    }
                });
            }
            else if(checker=="Images")
            {
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image Files");
                final String msgSenderRef="Messages/"+m_msgSenderID+"/"+m_msgReceiverID;
                final String msgReceiverRef="Messages/"+m_msgReceiverID+"/"+m_msgSenderID;
                DatabaseReference userMsgRef=m_RootRef.child("Messages").child(m_msgSenderID).child(m_msgReceiverID)
                        .push();
                final String msgPushID=userMsgRef.getKey();//this will generate a randrom new key everytime.
                final StorageReference filePath=storageReference.child(msgPushID+"."+"jpg");
                uploadTask=filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful())
                        {
                            throw new Exception("error");
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            Uri downloadUrl=task.getResult();
                            myUrl=downloadUrl.toString();
                            Map msgImageBody=new HashMap();
                            msgImageBody.put("message",myUrl);
                            msgImageBody.put("name",fileUri.getLastPathSegment());
                            msgImageBody.put("type",checker);
                            msgImageBody.put("from",m_msgSenderID);
                            msgImageBody.put("to", m_msgReceiverID);
                            msgImageBody.put("messageID", msgPushID);
                            msgImageBody.put("time", saveCurrentTime);
                            msgImageBody.put("date", saveCurrentDate);

                            Map msgBodyDetails=new HashMap();
                            msgBodyDetails.put(msgSenderRef+"/"+msgPushID,msgImageBody);
                            msgBodyDetails.put(msgReceiverRef+"/"+msgPushID,msgImageBody);
                            m_RootRef.updateChildren(msgBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Msg sent", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    m_msgInputText.setText("");
                                }
                            });
                        }
                    }
                });
            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing was selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeControllers() {

        m_chatToolbar=findViewById(R.id.chat_toolbar);
        setSupportActionBar(m_chatToolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView=layoutInflater.inflate(R.layout.custom_chat_bar,null);
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

        m_SendFilesButton = findViewById(R.id.send_files_btn);
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
        loadingBar=new ProgressDialog(this);
    }

    private void displayLastSeen()
    {
        m_RootRef.child("Users").child(m_msgReceiverID).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state=dataSnapshot.child("userState").child("state").getValue().toString();
                            String date=dataSnapshot.child("userState").child("date").getValue().toString();
                            String time=dataSnapshot.child("userState").child("time").getValue().toString();
                            if(state.equals("online"))
                            {
                                m_Lastseen.setText("online");
                            }
                            else if(state.equals("offline"))
                            {
                                m_Lastseen.setText("Last seen:"+" "+date+" "+time);
                            }
                        }
                        else {

                            m_Lastseen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_RootRef.child("Messages").child(m_msgSenderID).child(m_msgReceiverID).
                removeEventListener(childEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        m_RootRef.child("Messages").child(m_msgSenderID).child(m_msgReceiverID).
                removeEventListener(childEventListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        m_RootRef.child("Messages").child(m_msgSenderID).child(m_msgReceiverID)
                .addChildEventListener(childEventListener);
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
            msgTextBody.put("to", m_msgReceiverID);
            msgTextBody.put("messageID", msgPushID);
            msgTextBody.put("time", saveCurrentTime);
            msgTextBody.put("date", saveCurrentDate);

            Map msgBodyDetails=new HashMap();
            msgBodyDetails.put(msgSenderRef+"/"+msgPushID,msgTextBody);
            msgBodyDetails.put(msgReceiverRef+"/"+msgPushID,msgTextBody);
            m_RootRef.updateChildren(msgBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
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
