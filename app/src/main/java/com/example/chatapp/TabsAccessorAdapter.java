package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.chatapp.AllFragments.ChatWork.ChatsFragment;
import com.example.chatapp.AllFragments.ContactsFragment;
import com.example.chatapp.AllFragments.GroupChats.GroupsFragment;
import com.example.chatapp.AllFragments.RequestFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    public TabsAccessorAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                ChatsFragment chatsFragment=new ChatsFragment();
                return chatsFragment;
            case 1:
                ContactsFragment contactsFragment=new ContactsFragment();
                return contactsFragment;
            case 2:
                GroupsFragment groupsFragment=new GroupsFragment();
                return groupsFragment;
            case 3:
                RequestFragment requestFragment=new RequestFragment();
                return requestFragment;
                default:
                    return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
                return "Chats";
            case 1:
                return "Contacts";
            case 2:
                return "Groups";
            case 3:
                return "Requests";
                default:
                    return null;
        }
    }
}
