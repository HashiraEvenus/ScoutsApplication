package com.metropolitan.appchat.Models;

public class Message { // creates the message class
    private String messageId,message,senderId, imageUrl; //stores the following variables
    private long timestamp; //timestamp for time of message sent
    private int feel=-1; // IF NOT -1 THEN BY DEFAULT MESSAGE COMES WITH REACTION


//EMPTY CONSTRUCTOR

    public Message() {
    }

    //CONSTRUCTOR START

    public Message(String message, String senderId, long timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }
    public String getMessageId() {
        return messageId;
    }

    //GETTER SETTER START
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getFeel() {
        return feel;
    }

    public void setFeel(int feel) {
        this.feel = feel;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
