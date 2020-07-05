package com.example.chatapp.MenuActivities.FindFriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.chatapp.AllFragments.ModelClass.Contacts;
import com.example.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar m_toolBar;
    private RecyclerView m_findFreindsRecyclerView;
    private DatabaseReference m_usersRef;
    private FirebaseRecyclerAdapter<Contacts,FindFriendViewHolder> adapter;
    private FirebaseRecyclerOptions<Contacts> options;
    private SearchView searchView;
    private Set<String> m_UsersIds=new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        m_findFreindsRecyclerView=findViewById(R.id.find_friends_recycler_list);
        m_findFreindsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        m_toolBar=findViewById(R.id.find_friends_toolbar);
        m_usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        setSupportActionBar(m_toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        searchView= findViewById(R.id.searching_friends);

//        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

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
    }

    @Override
    protected void onStart() {
        super.onStart();

        options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(m_usersRef,Contacts.class).build();

        adapter=new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {
                holder.username.setText(model.getName());
                holder.userstatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);
                m_UsersIds.add(getRef(position).getKey());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visitUserId=getRef(position).getKey();
                        Intent intent=new Intent(FindFriendsActivity.this, ProfileActivity.class);
                        intent.putExtra("visitUserId",visitUserId);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                FindFriendViewHolder findFriendViewHolder=new FindFriendViewHolder(view);
                return findFriendViewHolder;
            }
        };
        m_findFreindsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView username,userstatus;
        CircleImageView profileImage;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_profile_name);
            userstatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
        }

    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        getMenuInflater().inflate(R.menu.search_view_menu,menu);
//
//        MenuItem searchItem=menu.findItem(R.id.search_view);
//        SearchView searchView= findViewById(R.id.searching_friends);
//
////        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                firebaseSearch(query);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                firebaseSearch(newText);
//                return false;
//            }
//        });
//
//        return true;
//    }

    private void firebaseSearch(String searchText)
    {

        final String query=searchText.toLowerCase();


        options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(m_usersRef,Contacts.class).build();

            adapter =new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull final FindFriendViewHolder holder, final int position, @NonNull final Contacts model) {
                    String userIds = getRef(position).getKey();
                    m_usersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String name=snapshot.child("name").getValue().toString();
                                if(name.contains(query))
                                {
                                    holder.username.setText(model.getName());
                                    holder.userstatus.setText(model.getStatus());
                                    Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String visitUserId=getRef(position).getKey();
                                            Intent intent=new Intent(FindFriendsActivity.this,ProfileActivity.class);
                                            intent.putExtra("visitUserId",visitUserId);
                                            startActivity(intent);
                                        }
                                    });
                                }
                                else {
                                    holder.itemView.setVisibility(View.GONE);
                                    holder.itemView.setScaleX(0);
                                    holder.itemView.setScaleY(0);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

                @NonNull
                @Override
                public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                    return new FindFriendViewHolder(view);
                }
            };
            m_findFreindsRecyclerView.setAdapter(adapter);
            adapter.startListening();
    }
}
