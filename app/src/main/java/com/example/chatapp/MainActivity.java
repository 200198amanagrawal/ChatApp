package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar m_toolbar;
    private ViewPager m_ViewPager;
    private TabLayout m_TabLayout;
    private TabsAccessorAdapter m_tabsAccessorAdapter;
    private FirebaseUser m_currentUser;
    private FirebaseAuth m_auth;
    private DatabaseReference m_dataBaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_auth=FirebaseAuth.getInstance();
        m_dataBaseReference= FirebaseDatabase.getInstance().getReference();
        m_currentUser=m_auth.getCurrentUser();
        m_toolbar =findViewById(R.id.main_page_toolbar);
        setSupportActionBar(m_toolbar);
        getSupportActionBar().setTitle("ChatApp");
        m_ViewPager=findViewById(R.id.main_tabs_pager);
        //tabs layout which is replaced with tabs
        m_tabsAccessorAdapter=new TabsAccessorAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        //the adapter we use in order to provide data to fragment.
        m_ViewPager.setAdapter(m_tabsAccessorAdapter);
        m_TabLayout=findViewById(R.id.main_tabs);
        //this will take all the tabs such as chats,contacts groups
        m_TabLayout.setupWithViewPager(m_ViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(m_currentUser==null)
        {
            startLoginActivity();
        }
        else {
            verifyUserAvailability();
        }
    }

    private void verifyUserAvailability() {
        String uid=m_auth.getCurrentUser().getUid();
        m_dataBaseReference.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists()))
                {
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else{
                    startSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startLoginActivity()
    {
        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId())
        {
            case R.id.logout:
                m_auth.signOut();
                startLoginActivity();
                break;
            case R.id.settings:
                startSettingsActivity();
                break;
            case R.id.find_friends:
                startFindFriendActivity();
                break;
            case R.id.create_groups:
                createNewGroups();
                break;
                default:
                    break;
        }
        return true;
    }

    private void startFindFriendActivity() {
        Intent intent=new Intent(MainActivity.this,FindFriendsActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void createNewGroups() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name");
        final EditText groupNameField=new EditText(MainActivity.this);
        groupNameField.setHint("e.g. Some cafe..");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName=groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this, "Please enter some group name", Toast.LENGTH_SHORT).show();
                }
                else {
                    createSingleGroup(groupName);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void createSingleGroup(final String groupName) {
        m_dataBaseReference.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, groupName+"is created sucessfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void startSettingsActivity() {
        Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(intent);
    }
}
