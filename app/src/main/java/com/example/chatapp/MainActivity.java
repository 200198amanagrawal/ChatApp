package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Toolbar m_toolbar;
    private ViewPager m_ViewPager;
    private TabLayout m_TabLayout;
    private TabsAccessorAdapter m_tabsAccessorAdapter;
    private FirebaseUser m_currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    }
    private void startLoginActivity()
    {
        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
    }
}
