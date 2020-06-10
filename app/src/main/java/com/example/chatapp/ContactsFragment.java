package com.example.chatapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
public class ContactsFragment extends Fragment {


    private View m_ContactsView;
    private RecyclerView m_myContactList;
    private DatabaseReference m_ContactRef,m_UsersRef;
    private FirebaseAuth mAuth;
    private String m_currentUserID;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        m_ContactsView= inflater.inflate(R.layout.fragment_contacts, container, false);
        m_myContactList=m_ContactsView.findViewById(R.id.contacts_list);
        m_myContactList.setLayoutManager(new LinearLayoutManager(getContext()));


        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null)
        {
            Intent intent=new Intent(getContext(),LoginActivity.class);
            startActivity(intent);
        }
        m_currentUserID=mAuth.getCurrentUser().getUid();
        m_ContactRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(m_currentUserID);
        m_UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        return m_ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(m_ContactRef,Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, final int position, @NonNull Contacts model) {
                String userIds=getRef(position).getKey();
                m_UsersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            if(dataSnapshot.child("userState").hasChild("state"))
                            {
                                String state=dataSnapshot.child("userState").child("state").getValue().toString();
                                if(state.equals("online"))
                                {
                                   holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if(state.equals("offline"))
                                {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }
                            else {

                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }
                            if(dataSnapshot.hasChild("image"))
                            {
                                String userImage=dataSnapshot.child("image").getValue().toString();
                                String profileStatus=dataSnapshot.child("status").getValue().toString();
                                String profileName=dataSnapshot.child("name").getValue().toString();
                                holder.username.setText(profileName);
                                holder.userstatus.setText(profileStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }
                            else
                            {
                                String profileStatus=dataSnapshot.child("status").getValue().toString();
                                String profileName=dataSnapshot.child("name").getValue().toString();
                                holder.username.setText(profileName);
                                holder.userstatus.setText(profileStatus);
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
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                ContactsViewHolder holder=new ContactsViewHolder(view);
                return holder;
            }
        };
        m_myContactList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView username,userstatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_profile_name);
            userstatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            onlineIcon=itemView.findViewById(R.id.user_online_status);
        }
    }
}
