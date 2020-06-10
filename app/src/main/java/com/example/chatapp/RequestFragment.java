package com.example.chatapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {


    private View m_RequestFragmentView;
    private RecyclerView m_RequestList;
    private DatabaseReference m_ChatRequestRef,m_UsersRef,m_ContactsRef;
    private FirebaseAuth mAuth;
    private String m_currentUserID;
    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        m_RequestFragmentView= inflater.inflate(R.layout.fragment_request, container, false);
        m_RequestList=m_RequestFragmentView.findViewById(R.id.chat_request_list);
        m_RequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        m_currentUserID=mAuth.getCurrentUser().getUid();
        m_ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        m_ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        m_UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        return m_RequestFragmentView;
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(m_ChatRequestRef.child(m_currentUserID),Contacts.class)
                        .build();
        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, final int position, @NonNull Contacts model)
                    {
                        holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_reject_button).setVisibility(View.VISIBLE);
                        final String list_user_id=getRef(position).getKey();
                        final DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();
                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists())
                                {
                                    String type=dataSnapshot.getValue().toString();
                                    if(type.equals("received"))
                                    {
                                        m_UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("image"))
                                                {
                                                    String requestImage=dataSnapshot.child("image").getValue().toString();
                                                    Picasso.get().load(requestImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                }
                                                    final String requestStatus=dataSnapshot.child("status").getValue().toString();
                                                    final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                                    holder.username.setText(requestUserName);
                                                    holder.userstatus.setText(requestStatus);
                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence optionToClick[]=new CharSequence[]
                                                                {
                                                                        "Accept","Cancel"
                                                                };
                                                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUserName+" Chat Request");
                                                        builder.setItems(optionToClick, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                if(which==0)
                                                                {
                                                                    m_ContactsRef.child(m_currentUserID).child(list_user_id).child("Contact")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()) {
                                                                                m_ContactsRef.child(list_user_id).child(m_currentUserID).child("Contact")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if(task.isSuccessful())
                                                                                        {
                                                                                            m_ChatRequestRef.child(m_currentUserID).child(list_user_id).removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if(task.isSuccessful())
                                                                                                    {
                                                                                                        m_ChatRequestRef.child(list_user_id).child(m_currentUserID).removeValue()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        if(task.isSuccessful())
                                                                                                                        {
                                                                                                                            Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();
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
                                                                    });
                                                                }
                                                                if(which==1)
                                                                {
                                                                    m_ChatRequestRef.child(m_currentUserID).child(list_user_id).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        m_ChatRequestRef.child(list_user_id).child(m_currentUserID).removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext(), "Request Rejected", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                    else if(type.equals("sent"))
                                    {
                                        Button request_sent_btn=holder.itemView.findViewById(R.id.request_accept_button);
                                        request_sent_btn.setText("Req Sent");
                                        holder.itemView.findViewById(R.id.request_reject_button).setVisibility(View.INVISIBLE);
                                        m_UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("image"))
                                                {
                                                    String requestImage=dataSnapshot.child("image").getValue().toString();
                                                    Picasso.get().load(requestImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                }
                                                final String requestStatus=dataSnapshot.child("status").getValue().toString();
                                                final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                                holder.username.setText(requestUserName);
                                                holder.userstatus.setText("You have sent a request");
                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence optionToClick[]=new CharSequence[]
                                                                {
                                                                        "Cancel Chat Request"
                                                                };
                                                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Already Sent Request");
                                                        builder.setItems(optionToClick, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                if(which==0)
                                                                {
                                                                    m_ChatRequestRef.child(m_currentUserID).child(list_user_id).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        m_ChatRequestRef.child(list_user_id).child(m_currentUserID).removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext(), "Cancelled the Request", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override//this function is used to inflate the layout which contains the user name and status and will show to recyclerview
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        RequestsViewHolder holder=new RequestsViewHolder(view);
                        return holder;
                    }
                };
        m_RequestList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        TextView username,userstatus;
        CircleImageView profileImage;
        Button AcceptButton,RejectButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_profile_name);
            userstatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            AcceptButton=itemView.findViewById(R.id.request_accept_button);
            RejectButton=itemView.findViewById(R.id.request_reject_button);
        }
    }
}
