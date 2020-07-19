package com.example.chatapp.AllFragments.ModelClass;

public class Messages
{
    private String from, message, type, to, messageID, time, date, name,groupID,sentOrReceived;

    public Messages()
    {

    }

    public Messages(String from, String message, String type, String to, String messageID, String time, String date, String name) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageID = messageID;
        this.time = time;
        this.date = date;
        this.name = name;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupID() {
        return groupID;
    }

    public Messages(String from, String message, String type, String to, String messageID
            , String time, String date, String name, String groupID,String sentOrReceived) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageID = messageID;
        this.time = time;
        this.date = date;
        this.name = name;
        this.groupID = groupID;
        this.sentOrReceived=sentOrReceived;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getSentOrReceived() {
        return sentOrReceived;
    }

    public void setSentOrReceived(String sentOrReceived) {
        this.sentOrReceived = sentOrReceived;
    }
}
