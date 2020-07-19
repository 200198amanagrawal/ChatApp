package com.example.chatapp.AllFragments.ChatWork;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.chatapp.AllFragments.ModelClass.Contacts;
import com.example.chatapp.R;
import com.example.chatapp.SignupAndLogin.LoginActivity;
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
public class ChatsFragment extends Fragment {

    private View m_PrivateChatView;
    private RecyclerView m_chatsList;
    private DatabaseReference m_ChatsRef,m_UsersRef;
    private FirebaseAuth mAuth;
    private String m_CurrentUserID;
    private SearchView searchView;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        m_PrivateChatView= inflater.inflate(R.layout.fragment_chats, container, false);
        m_chatsList=m_PrivateChatView.findViewById(R.id.chats_list);
        m_chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null)
        {
            Intent intent=new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
        }
        else {
            m_CurrentUserID = mAuth.getCurrentUser().getUid();
            m_ChatsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(m_CurrentUserID);

        }
        m_UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        searchView=m_PrivateChatView.findViewById(R.id.searching_chat);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                firebaseSearch(newText);
                return false;
            }
        });


        return m_PrivateChatView;
    }



    @Override
    public void onStart() {
        super.onStart();
        if(m_ChatsRef!=null)
        {
        FirebaseRecyclerOptions options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(m_ChatsRef,Contacts.class)
                        .build();
        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                        //this function is bascially ran in a loop so that postion works as a Firebase database postion
                        final String userIds=getRef(position).getKey();//this will get the id of current user's contact list at a particular postion
                        final String[] retImage = {"default_image"};

                        m_UsersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists())
                                {

                                    if(dataSnapshot.hasChild("image"))
                                    {
                                        retImage[0] =dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                    }
                                    final String retName=dataSnapshot.child("name").getValue().toString();
                                    holder.username.setText(retName);
                                    if(dataSnapshot.child("userState").hasChild("state"))
                                    {
                                        String state=dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date=dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time=dataSnapshot.child("userState").child("time").getValue().toString();
                                        if(state.equals("online"))
                                        {
                                            holder.userstatus.setText("online");
                                        }
                                        else if(state.equals("offline"))
                                        {
                                            holder.userstatus.setText("Last seen:"+" "+date+" "+time);
                                        }
                                    }
                                    else {

                                        holder.userstatus.setText("offline");
                                    }

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent=new Intent(getContext(), ChatActivity.class);
                                            intent.putExtra("visit_user_id",userIds);
                                            intent.putExtra("visit_user_name",retName);
                                            intent.putExtra("visit_user_image", retImage[0]);
                                            startActivity(intent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        ChatsViewHolder holder=new ChatsViewHolder(view);
                        return holder;
                    }
                };
        m_chatsList.setAdapter(adapter);
        adapter.startListening();
        }
    }
    public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        TextView username,userstatus;
        CircleImageView profileImage;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_profile_name);
            userstatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
        }
    }
    private void firebaseSearch(String searchText) {
        if(m_ChatsRef!=null)
        {
            final String query=searchText.toLowerCase();
            FirebaseRecyclerOptions options=
                    new FirebaseRecyclerOptions.Builder<Contacts>()
                            .setQuery(m_ChatsRef,Contacts.class)
                            .build();
            FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter=
                    new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                            //this function is bascially ran in a loop so that postion works as a Firebase database postion
                            final String userIds=getRef(position).getKey();//this will get the id of current user's contact list at a particular postion
                            final String[] retImage = {"default_image"};

                            m_UsersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {
                                        String name = dataSnapshot.child("name").getValue().toString();
                                        if (name.contains(query)) {
                                            if (dataSnapshot.hasChild("image")) {
                                                retImage[0] = dataSnapshot.child("image").getValue().toString();
                                                Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                            }
                                            String retStatus = dataSnapshot.child("status").getValue().toString();
                                            final String retName = dataSnapshot.child("name").getValue().toString();
                                            holder.username.setText(retName);

                                            if (dataSnapshot.child("userState").hasChild("state")) {
                                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                                String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                                if (state.equals("online")) {
                                                    holder.userstatus.setText("online");
                                                } else if (state.equals("offline")) {
                                                    holder.userstatus.setText("Last seen:" + " " + date + " " + time);
                                                }
                                            } else {

                                                holder.userstatus.setText("offline");
                                            }

                                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent(getContext(), ChatActivity.class);
                                                    intent.putExtra("visit_user_id", userIds);
                                                    intent.putExtra("visit_user_name", retName);
                                                    intent.putExtra("visit_user_image", retImage[0]);
                                                    startActivity(intent);
                                                }
                                            });
                                        }
                                        else {
                                            holder.itemView.setVisibility(View.GONE);
                                            View view=holder.itemView;
                                            ViewGroup.LayoutParams layoutParams=view.getLayoutParams();
                                            layoutParams.width=0;
                                            layoutParams.height=0;
                                            view.setLayoutParams(layoutParams);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                        @NonNull
                        @Override
                        public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                            ChatsViewHolder holder=new ChatsViewHolder(view);
                            return holder;
                        }
                    };
            m_chatsList.setAdapter(adapter);
            adapter.startListening();
        }
    }
}
