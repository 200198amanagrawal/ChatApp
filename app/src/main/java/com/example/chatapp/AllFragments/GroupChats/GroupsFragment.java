package com.example.chatapp.AllFragments.GroupChats;


import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chatapp.AllFragments.ModelClass.GroupDetails;
import com.example.chatapp.Group_ChatActivity;
import com.example.chatapp.MainActivity;
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

import java.util.ArrayList;
import java.util.HashMap;

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
    ArrayList<String> userIDs;
    private String groupName;
    private String groupID;

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
      //      m_GroupsChatRef = FirebaseDatabase.getInstance().getReference().child("GroupChat");
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
                final String groupRefID=getRef(position).getKey();
                m_GroupsRef.child(groupRefID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot snapshot) {
                        if(snapshot.exists() && snapshot.child("userIDs").hasChild(m_CurrentUserID))
                        {
                            holder.username.setText(model.getGroupName());
                            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(30,40,0,0);
                            holder.username.setLayoutParams(params);
                            holder.userstatus.setText("");
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent=new Intent(getContext(), Group_ChatActivity.class);
                                    intent.putExtra("userIDs",(HashMap<String,Object>)snapshot.child("userIDs").getValue());
                                    intent.putExtra("groupName",model.getGroupName());
                                    intent.putExtra("groupID",groupRefID);
                                    startActivity(intent);
                                }
                            });
                        }
                        else {
                            hideExtraGroupDetails(holder);
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

    private void hideExtraGroupDetails(@NonNull GroupFragmentHolder holder) {
        holder.itemView.setVisibility(View.GONE);
        View view=holder.itemView;
        ViewGroup.LayoutParams layoutParams=view.getLayoutParams();
        layoutParams.width=0;
        layoutParams.height=0;
        view.setLayoutParams(layoutParams);
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
