package com.dsa.chat.api.chatapp.Components;

public class MessagePojo {
    
    private String message;
    private String sender;
    private String sessionId;
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public MessagePojo(String message, String sender, String sessionId) {
        this.message = message;
        this.sender = sender;
        this.sessionId = sessionId;
    }

}
