package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private Toolbar m_toolbar;
    private ViewPager m_ViewPager;
    private TabLayout m_TabLayout;
    private TabsAccessorAdapter m_tabsAccessorAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_toolbar =findViewById(R.id.main_page_toolbar);
        setSupportActionBar(m_toolbar);
        getSupportActionBar().setTitle("ChatApp");
        m_ViewPager=findViewById(R.id.main_tabs_pager);
        m_tabsAccessorAdapter=new TabsAccessorAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        m_ViewPager.setAdapter(m_tabsAccessorAdapter);
        m_TabLayout=findViewById(R.id.main_tabs);
        m_TabLayout.setupWithViewPager(m_ViewPager);
    }
}
