package com.dev.model;

// com.dev.model.ChatMessage.java
public class ChatMessage {
    private String senderId;
    private String text;
    private com.google.firebase.Timestamp timestamp;

    public ChatMessage() { }  // Required

    public ChatMessage(String senderId, String text, com.google.firebase.Timestamp timestamp) {
        this.senderId = senderId;
        this.text     = text;
        this.timestamp= timestamp;
    }

    public String getSenderId()      { return senderId; }
    public String getText()          { return text;     }
    public com.google.firebase.Timestamp getTimestamp() { return timestamp; }
}
