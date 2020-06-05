package com.example.chatapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<Messages> userMsgList;
    private FirebaseAuth mAuth;
    private DatabaseReference m_UserRef;

    public MessagesAdapter (List<Messages> userMsgList)
    {
        this.userMsgList=userMsgList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMsgText,receiverMsgText;
        public CircleImageView receiverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsgText=itemView.findViewById(R.id.sender_message_text);
            receiverMsgText=itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage=itemView.findViewById(R.id.receiver_profile_image);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout,parent,false);
        mAuth=FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        String messageSenderId=mAuth.getCurrentUser().getUid();
        Messages messages=userMsgList.get(position);

        String fromUserID=messages.getFrom();
        String fromMessageType=messages.getType();
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

        if(fromMessageType.equals("text"))
        {
            holder.receiverMsgText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            holder.senderMsgText.setVisibility(View.INVISIBLE);
            if(fromUserID.equals(messageSenderId))
            {
                holder.senderMsgText.setVisibility(View.VISIBLE);
                holder.senderMsgText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMsgText.setText(messages.getMessage());
            }
            else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMsgText.setVisibility(View.VISIBLE);
                holder.receiverMsgText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMsgText.setText(messages.getMessage());
            }
        }

    }

    @Override
    public int getItemCount() {
        return userMsgList.size();
    }


}
