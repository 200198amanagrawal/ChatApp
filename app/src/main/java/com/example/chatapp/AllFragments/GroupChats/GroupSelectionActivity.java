package com.example.chatapp.AllFragments.GroupChats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.AllFragments.ModelClass.Contacts;
import com.example.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupSelectionActivity extends AppCompatActivity {

    private Toolbar m_toolBar;
    private RecyclerView m_findFreindsRecyclerView;
    private DatabaseReference m_usersRef,m_ContactRef;
    private String m_currentUserID;
    private FirebaseAuth mAuth;
    private FirebaseRecyclerAdapter<Contacts, GroupContactViewHolder> adapter;
    private FirebaseRecyclerOptions<Contacts> options;
    private SearchView searchView;
    private Set<String> m_UsersIds=new HashSet<>();
    private TextView m_SelectedContactsName;
    public HashSet<String> m_GroupUserIds=new HashSet<>();
    private CircleImageView m_SendUserDetails;
    public HashSet<String> m_GroupUserNames=new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_selection);
        m_findFreindsRecyclerView=findViewById(R.id.find_group_contact_recycler_list);
        m_findFreindsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        m_SelectedContactsName=findViewById(R.id.selected_contacts);
        m_toolBar=findViewById(R.id.find_group_contact_toolbar);
        mAuth = FirebaseAuth.getInstance();
        m_usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        m_currentUserID = mAuth.getCurrentUser().getUid();
        m_ContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(m_currentUserID);
        setSupportActionBar(m_toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Select Friends");

        searchView= findViewById(R.id.searching_group_contact);


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
        m_SendUserDetails=findViewById(R.id.send_group_users_data);
        m_SendUserDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m_GroupUserIds.size()==0)
                {
                    Toast.makeText(GroupSelectionActivity.this, "Select some contact", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent=new Intent(GroupSelectionActivity.this,GroupDetailsActivity.class);
                    intent.putExtra("UserIds",m_GroupUserIds);
                    startActivity(intent);
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(m_ContactRef,Contacts.class).build();

        adapter=new FirebaseRecyclerAdapter<Contacts, GroupContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final GroupContactViewHolder holder, final int position, @NonNull final Contacts model) {
                final String userId = getRef(position).getKey();
                m_usersRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            if (dataSnapshot.hasChild("image"))
                            {
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }
                            String profileStatus = dataSnapshot.child("status").getValue().toString();
                            final String profileName = dataSnapshot.child("name").getValue().toString();
                            final String userContactID=dataSnapshot.getKey();
                            holder.username.setText(profileName);
                            holder.userstatus.setText(profileStatus);
                            holder.checkBox.setVisibility(View.VISIBLE);
                            m_UsersIds.add(getRef(position).getKey());
                            if(m_GroupUserIds.contains(userContactID))
                            {
                                holder.checkBox.setChecked(true);
                            }
                            else {
                                holder.checkBox.setChecked(false);
                            }
                            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(holder.checkBox.isChecked())
                                    {
                                        m_GroupUserIds.add(userContactID);
                                        m_GroupUserNames.add(profileName);
                                        m_SelectedContactsName.setText("");
                                        if(m_GroupUserNames!=null) {
                                            for (String name : m_GroupUserNames) {
                                                m_SelectedContactsName.append(name+" ");
                                            }
                                        }

                                    }
                                    else if(!holder.checkBox.isChecked() && (m_GroupUserIds!=null))
                                    {
                                        m_GroupUserIds.remove(userContactID);
                                        m_GroupUserNames.remove(profileName);
                                        m_SelectedContactsName.setText("");
                                        for(String name:m_GroupUserNames)
                                        {
                                            m_SelectedContactsName.append(name+" ");
                                        }
                                    }
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            @NonNull
            @Override
            public GroupContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                GroupContactViewHolder findFriendViewHolder=new GroupContactViewHolder(view);
                return findFriendViewHolder;
            }
        };
        m_findFreindsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class GroupContactViewHolder extends RecyclerView.ViewHolder
    {
        TextView username,userstatus;
        CircleImageView profileImage;
        CheckBox checkBox;

        public GroupContactViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_profile_name);
            userstatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            checkBox=itemView.findViewById(R.id.select_data);
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_view_menu,menu);
        MenuItem searchItem=menu.findItem(R.id.search_view);
        SearchView searchView= (SearchView) searchItem.getActionView();


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

        return true;
    }

    private void firebaseSearch(String searchText)
    {

        final String query=searchText.toLowerCase();


        options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(m_ContactRef,Contacts.class).build();

        adapter =new FirebaseRecyclerAdapter<Contacts, GroupContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final GroupContactViewHolder holder, final int position, @NonNull final Contacts model) {
                final String userIds = getRef(position).getKey();
                m_usersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name=snapshot.child("name").getValue().toString();
                            if(name.contains(query))
                            {
                                if (snapshot.hasChild("image")) {
                                    String userImage = snapshot.child("image").getValue().toString();
                                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                }
                                String profileStatus = snapshot.child("status").getValue().toString();
                                final String profileName = snapshot.child("name").getValue().toString();
                                final String userContactID=snapshot.getKey();
                                holder.username.setText(profileName);
                                holder.checkBox.setVisibility(View.VISIBLE);
                                holder.userstatus.setText(profileStatus);
                                m_UsersIds.add(getRef(position).getKey());

                                if(m_GroupUserIds.contains(userContactID))
                                {
                                    holder.checkBox.setChecked(true);
                                }
                                else {
                                    holder.checkBox.setChecked(false);
                                }
                                holder.checkBox.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(holder.checkBox.isChecked())
                                        {

                                            m_GroupUserIds.add(userContactID);
                                            m_GroupUserNames.add(profileName);
                                            m_SelectedContactsName.setText("");
                                            if(m_GroupUserNames!=null) {
                                                for (String nameContact : m_GroupUserNames) {
                                                    m_SelectedContactsName.append(nameContact+" ");
                                                }
                                            }

                                        }
                                        else if(!holder.checkBox.isChecked() && (m_GroupUserIds!=null))
                                        {
                                            m_GroupUserIds.remove(userContactID);
                                            m_GroupUserNames.remove(profileName);
                                            m_SelectedContactsName.setText("");
                                            for(String nameContact:m_GroupUserNames)
                                            {
                                                m_SelectedContactsName.append(nameContact+" ");
                                            }
                                        }
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
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public GroupContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                return new GroupContactViewHolder(view);
            }
        };
        m_findFreindsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }
}
