package com.example.chatapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

private View groupFragmentView;
private ListView listView;
private ArrayAdapter<String> arrayAdapter;
private ArrayList<String> listOfGroups=new ArrayList<>();
private DatabaseReference m_GroupsRef;
    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupFragmentView= inflater.inflate(R.layout.fragment_groups, container, false);
        m_GroupsRef= FirebaseDatabase.getInstance().getReference().child("Groups");
        InitializeFields();
        RetrieveAndDisplayGroups();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currwentGroupName=parent.getItemAtPosition(position).toString();
                Intent groupChatIntent=new Intent(getContext(),GroupChatActivity.class);
                groupChatIntent.putExtra("groupName",currwentGroupName);
                startActivity(groupChatIntent);
            }
        });
        return groupFragmentView;
    }

    private void RetrieveAndDisplayGroups() {
        m_GroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set=new HashSet<>();
                Iterator iterator=dataSnapshot.getChildren().iterator();
                while (iterator.hasNext())
                {
                    set.add(((DataSnapshot)iterator.next()).getKey());//will get all the group keys
                }
                listOfGroups.clear();
                listOfGroups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields() {
        listView=groupFragmentView.findViewById(R.id.chatGroups);
        arrayAdapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,listOfGroups);
        listView.setAdapter(arrayAdapter);
    }

}
