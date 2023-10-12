package com.dsa.chat.api.chatapp.Components;

public class WsEvent {
    private EventType eventType;
    private String username;
    private String message;

    public WsEvent(EventType eventType, String username, String message) {
        this.eventType = eventType;
        this.username = username;
        this.message = message;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    
}
