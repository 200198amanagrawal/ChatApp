package com.example.chatapp.AllFragments.ModelClass;

public class GroupDetails {
    public String getGroupIcon() {
        return groupIcon;
    }

    public GroupDetails() {
    }

    public GroupDetails(String groupIcon, String groupName) {
        this.groupIcon = groupIcon;
        this.groupName = groupName;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    String groupIcon;
    String groupName;

}
