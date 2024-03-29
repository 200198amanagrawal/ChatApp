package com.example.chatapp.AllFragments.GroupChats;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.AllFragments.ChatWork.ImageViewerActivity;
import com.example.chatapp.AllFragments.ModelClass.Messages;
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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessagesAdapter extends RecyclerView.Adapter<GroupMessagesAdapter.MessageViewHolder> {

    private List<Messages> userMsgList;
    private FirebaseAuth mAuth;
    private DatabaseReference m_UserRef;


    public GroupMessagesAdapter (List<Messages> userMsgList)
    {
        this.userMsgList=userMsgList;
        setHasStableIds(true);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMsgText,receiverMsgText,senderImgOrDocName,receiverImgOrDocName;
        public CircleImageView receiverProfileImage;

        public LinearLayout m_MessageSenderPictureLayout, m_MessageReceiverPictureLayout;
        public ImageView m_MessageSenderPicture,m_MessageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsgText=itemView.findViewById(R.id.sender_message_text);
            receiverMsgText=itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage=itemView.findViewById(R.id.receiver_profile_image);
            m_MessageReceiverPictureLayout = itemView.findViewById(R.id.message_receiver_image_view);
            m_MessageSenderPictureLayout = itemView.findViewById(R.id.message_sender_image_view);
            m_MessageSenderPicture=itemView.findViewById(R.id.sender_message_doc_or_image);
            m_MessageReceiverPicture=itemView.findViewById(R.id.receiver_message_doc_or_image);
            senderImgOrDocName=itemView.findViewById(R.id.sender_image_name);
            receiverImgOrDocName=itemView.findViewById(R.id.receiver_image_name);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout,parent,false);
        mAuth=FirebaseAuth.getInstance();

        MessageViewHolder holder=new MessageViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        String messageSenderId=mAuth.getCurrentUser().getUid();
        Messages messages=userMsgList.get(position);

        String fromUserID=messages.getFrom();
        String sentOrReceived=messages.getSentOrReceived();
        String fromMessageType=messages.getType();
        String filename=messages.getName();

        m_UserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        m_UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("image"))
                {
                    String receiverImage=dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.receiverMsgText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMsgText.setVisibility(View.GONE);
        holder.m_MessageSenderPictureLayout.setVisibility(View.GONE);
        holder.m_MessageSenderPicture.setVisibility(View.GONE);
        holder.m_MessageReceiverPictureLayout.setVisibility(View.GONE);
        holder.m_MessageReceiverPicture.setVisibility(View.GONE);
        holder.senderImgOrDocName.setVisibility(View.GONE);
        holder.receiverImgOrDocName.setVisibility(View.GONE);

        if(fromMessageType.equals("text"))
        {
            if(sentOrReceived.equals("sent"))
            {
                holder.senderMsgText.setVisibility(View.VISIBLE);
                holder.senderMsgText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMsgText.setText(messages.getMessage()+"\n \n"+messages.getDate()+" - "+messages.getTime());
            }
            else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMsgText.setVisibility(View.VISIBLE);
                holder.receiverMsgText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMsgText.setText(messages.getMessage()+"\n \n"+messages.getDate()+" - "+messages.getTime());
            }
        }
        else if(fromMessageType.equals("Images"))
        {
            if(sentOrReceived.equals("sent"))
            {
                holder.m_MessageSenderPictureLayout.setVisibility(View.VISIBLE);
                holder.m_MessageSenderPicture.setVisibility(View.VISIBLE);
                holder.senderImgOrDocName.setVisibility(View.VISIBLE);
                holder.senderImgOrDocName.setText(filename+".jpg");
                holder.m_MessageReceiverPicture.setBackgroundResource(R.drawable.send_image);
                //   Picasso.get().load(messages.getMessage()).into(holder.m_MessageSenderPictureLayout);

            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.m_MessageReceiverPictureLayout.setVisibility(View.VISIBLE);
                holder.m_MessageReceiverPicture.setVisibility(View.VISIBLE);
                holder.receiverImgOrDocName.setVisibility(View.VISIBLE);
                holder.receiverImgOrDocName.setText(filename+".jpg");
                holder.m_MessageReceiverPicture.setBackgroundResource(R.drawable.send_image);
                //    Picasso.get().load(messages.getMessage()).into(holder.m_MessageReceiverPictureLayout);

            }
        }
        else
        {
            if(sentOrReceived.equals("sent"))
            {
                holder.m_MessageSenderPictureLayout.setVisibility(View.VISIBLE);
                holder.m_MessageSenderPicture.setVisibility(View.VISIBLE);
                holder.senderImgOrDocName.setVisibility(View.VISIBLE);
                holder.senderImgOrDocName.setText(filename);
                holder.m_MessageSenderPicture.setBackgroundResource(R.drawable.send_file);
            }
            else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.m_MessageReceiverPictureLayout.setVisibility(View.VISIBLE);
                holder.m_MessageReceiverPicture.setVisibility(View.VISIBLE);
                holder.receiverImgOrDocName.setVisibility(View.VISIBLE);
                holder.receiverImgOrDocName.setText(filename);
                holder.m_MessageReceiverPicture.setBackgroundResource(R.drawable.send_file);

            }
        }
        if(sentOrReceived.equals("sent"))
        {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(userMsgList.get(position).getType().equals("pdf") ||userMsgList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and View this doc",
                                        "Cancel",
                                        "Delete for everyone"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("What to do?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0)
                                {
                                    deleteSendMsg(position,holder);
                                }
                                else if(which==1)
                                {
                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMsgList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(which==3)
                                {
                                    deleteMsgForEveryone(position,holder);
                                }

                            }
                        });
                        builder.show();

                    }
                    else if(userMsgList.get(position).getType().equals("text"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel",
                                        "Delete for everyone"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("What to do?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0)
                                {
                                    deleteSendMsg(position,holder);
                                }

                                else if(which==2)
                                {
                                    deleteReceiveMsg(position,holder);
                                }

                            }
                        });
                        builder.show();

                    }
                    else if(userMsgList.get(position).getType().equals("Images"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "View this image",
                                        "Cancel",
                                        "Delete for everyone"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("What to do?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0)
                                {
                                    deleteSendMsg(position,holder);
                                }
                                else if(which==1)
                                {
                                    Intent intent=new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url",userMsgList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(which==3)
                                {
                                    deleteMsgForEveryone(position,holder);
                                }

                            }
                        });
                        builder.show();

                    }
                    return false;
                }
            });
        }
        else
        {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userMsgList.get(position).getType().equals("pdf") ||userMsgList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and View this doc",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("What to do?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0)
                                {
                                    deleteReceiveMsg(position,holder);
                                }
                                else if(which==1)
                                {
                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMsgList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();

                    }
                    else if(userMsgList.get(position).getType().equals("text"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("What to do?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0)
                                {
                                    deleteReceiveMsg(position,holder);
                                }

                            }
                        });
                        builder.show();

                    }
                    else if(userMsgList.get(position).getType().equals("Images"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "View this image",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("What to do?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0)
                                {
                                    deleteReceiveMsg(position,holder);
                                }
                                else if(which==1)
                                {
                                    Intent intent=new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url",userMsgList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();

                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userMsgList.size();
    }

    private void deleteSendMsg(final int postion,final MessageViewHolder holder)
    {
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("GroupMessages")
                .child(userMsgList.get(postion).getGroupID())
                .child(userMsgList.get(postion).getTo())
                .child(userMsgList.get(postion).getMessageID())
                .removeValue();

    }
    private void deleteReceiveMsg(final int postion,final MessageViewHolder holder)
    {
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("GroupMessages")
                .child(userMsgList.get(postion).getGroupID())
                .child(userMsgList.get(postion).getTo())
                .child(userMsgList.get(postion).getFrom())
                .child(userMsgList.get(postion).getMessageID())
                .removeValue();

    }
    private void deleteMsgForEveryone(final int postion,final MessageViewHolder holder)
    {
        final DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("GroupMessages")
                .child(userMsgList.get(postion).getGroupID())
                .child(userMsgList.get(postion).getFrom())
                .child(userMsgList.get(postion).getTo())
                .child(userMsgList.get(postion).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    rootRef.child("GroupMessages")
                            .child(userMsgList.get(postion).getGroupID())
                            .child(userMsgList.get(postion).getTo())
                            .child(userMsgList.get(postion).getFrom())
                            .child(userMsgList.get(postion).getMessageID())
                            .removeValue();
                }

            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
