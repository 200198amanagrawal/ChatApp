package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.chatapp.AllFragments.GroupChats.GroupSelectionActivity;
import com.example.chatapp.MenuActivities.FindFriends.FindFriendsActivity;
import com.example.chatapp.MenuActivities.ShowSettings;
import com.example.chatapp.MenuActivities.UpdateProfileActivity;
import com.example.chatapp.SignupAndLogin.LoginActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar m_toolbar;
    private ViewPager m_ViewPager;
    private TabLayout m_TabLayout;
    private TabsAccessorAdapter m_tabsAccessorAdapter;
    private FirebaseAuth m_auth;
    private DatabaseReference m_dataBaseReference;
    private String m_CurrentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_auth=FirebaseAuth.getInstance();

        m_dataBaseReference= FirebaseDatabase.getInstance().getReference();

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
        FirebaseUser m_currentUser=m_auth.getCurrentUser();
        if(m_currentUser==null)
        {
            startLoginActivity();
        }
        else {
            updateUserOnlineStatus("online");
            verifyUserAvailability();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser m_currentUser=m_auth.getCurrentUser();
        if(m_currentUser!=null)
        {
            updateUserOnlineStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser m_currentUser=m_auth.getCurrentUser();
        if(m_currentUser!=null)
        {
            updateUserOnlineStatus("offline");
        }
    }

    private void verifyUserAvailability() {
        String uid=m_auth.getCurrentUser().getUid();
        m_dataBaseReference.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(dataSnapshot.child("name").exists()))
                {
                    startUpdateProfileActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startLoginActivity()
    {
        Intent intent=new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
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
                updateUserOnlineStatus("offline");
                m_auth.signOut();
                startLoginActivity();
                break;
            case R.id.settings:
                updateUserOnlineStatus("online");
                startShowSettingsActivity();
                break;
            case R.id.find_friends:
                updateUserOnlineStatus("online");
                startFindFriendActivity();
                break;
            case R.id.create_groups:
                updateUserOnlineStatus("online");
                Intent intent=new Intent(MainActivity.this, GroupSelectionActivity.class);
                startActivity(intent);
                break;
                default:
                    break;
        }
        return true;
    }

    private void startFindFriendActivity() {
        Intent intent=new Intent(MainActivity.this, FindFriendsActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void startUpdateProfileActivity() {
        Intent intent=new Intent(MainActivity.this, UpdateProfileActivity.class);
        startActivity(intent);
    }
    private void startShowSettingsActivity() {
        Intent intent=new Intent(MainActivity.this, ShowSettings.class);
        startActivity(intent);
    }

    private void updateUserOnlineStatus(String state)
    {
        String saveCurrentTime,saveCurrentDate;
        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);


        m_CurrentUserID=m_auth.getCurrentUser().getUid();

        m_dataBaseReference.child("Users").child(m_CurrentUserID).child("userState")
                .updateChildren(onlineStateMap);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
