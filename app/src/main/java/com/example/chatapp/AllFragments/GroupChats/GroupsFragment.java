package com.example.chatapp.AllFragments.GroupChats;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.AllFragments.ModelClass.GroupDetails;
import com.example.chatapp.R;
import com.example.chatapp.SignupAndLogin.LoginActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private static final String TAG ="MYTAG" ;
    private View groupFragmentView;
    private DatabaseReference m_GroupsRef;
    private RecyclerView m_findFreindsRecyclerView;
    private FirebaseAuth mAuth;
    private SearchView searchView;
    private FirebaseRecyclerAdapter<GroupDetails, GroupFragmentHolder> adapter;
    private FirebaseRecyclerOptions<GroupDetails> options;
    String m_CurrentUserID;
    private DatabaseReference m_GroupsChatRef;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        mAuth = FirebaseAuth.getInstance();

        m_findFreindsRecyclerView=groupFragmentView.findViewById(R.id.searchingGroups);
        m_findFreindsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
        }
        else {
            m_CurrentUserID = mAuth.getCurrentUser().getUid();
            m_GroupsRef = FirebaseDatabase.getInstance().getReference().child("GroupDetails");
            m_GroupsChatRef = FirebaseDatabase.getInstance().getReference().child("GroupChat");
        }
        searchView=groupFragmentView.findViewById(R.id.searching_contacts);
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
        return groupFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        options=new FirebaseRecyclerOptions.Builder<GroupDetails>()
                .setQuery(m_GroupsRef,GroupDetails.class).build();

        adapter=new FirebaseRecyclerAdapter<GroupDetails, GroupFragmentHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final GroupFragmentHolder holder, final int position, @NonNull final GroupDetails model) {
                String groupRefID=getRef(position).getKey();
                m_GroupsChatRef.child(groupRefID).child(m_CurrentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            holder.username.setText(model.getGroupName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public GroupFragmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                GroupFragmentHolder findFriendViewHolder=new GroupFragmentHolder(view);
                return findFriendViewHolder;
            }
        };
        m_findFreindsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class GroupFragmentHolder extends RecyclerView.ViewHolder
    {
        TextView username,userstatus;
        CircleImageView profileImage;

        public GroupFragmentHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_profile_name);
            userstatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
        }

    }

    private void firebaseSearch(String query) {
    }


}
