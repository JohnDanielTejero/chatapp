package com.dsa.chat.api.chatapp.Components;

import java.security.Principal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class WsHandler {

    private Hashtable<String, String> users = new Hashtable<>();

    private final Queue<RegistrationRequest> registrationQueue = new LinkedList<>();
    private final Queue<WsEvent> eventQueue = new LinkedList<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/register-user")
    @SendToUser("/topic/registration-status")
    public String registerUser(Map<String, String> registrationData, StompHeaderAccessor accessor, Principal principal) throws JsonProcessingException {
        // Create a registration request object
        String username = registrationData.get("username"); // Daniel
        String sessionId = accessor.getSessionId(); //yung random letter
        RegistrationRequest request = new RegistrationRequest(username, sessionId);

        registrationQueue.offer(request);
        String registrationResponse = processRegistrationRequest(request); // "{"Success" : "Daniel"}" 
        if (registrationResponse.contains("Success")) {
            sendUserJoinMessage(username);
        }
      
        //messagingTemplate.convertAndSendToUser(username, "/user/topic/registration-status", registrationResponse);
        return registrationResponse;
    }


    private void sendUserJoinMessage(String username) {
        String message = username + " has joined the chat!";
        eventQueue.offer(new WsEvent(EventType.USER_JOIN, "Server", message)); 
    }

    private String processRegistrationRequest(RegistrationRequest request) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> response = new HashMap<>();
        synchronized (users) {
            if (users.containsKey(request.getUsername())) {
                response.put("Fail", "User already registered");
            } else {
                users.put(request.getUsername(), request.getSessionId());
                response.put("Success", request.getUsername());
            }
        }
        
        return objectMapper.writeValueAsString(response);
    }

   
    @MessageMapping("/chat")
    public void handleChatMessage(Map<String, String> messageData, StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String username = getUsernameForSession(sessionId);
        String message = messageData.get("message");
        
        if (username != null) {
            eventQueue.offer(new WsEvent(EventType.CHAT_MESSAGE, username, message));
        }

    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
    
        System.out.println("User disconnected with session ID: " + sessionId);
    
        synchronized (users) {
            String disconnectedUsername = null;
            for (Map.Entry<String, String> entry : users.entrySet()) {
                if (sessionId.equals(entry.getValue())) {
                    disconnectedUsername = entry.getKey();
                    users.remove(entry.getKey());
                    break; // Exit the loop after removal
                }
            }
            if (disconnectedUsername != null) {
                sendUserDisconnectMessage(disconnectedUsername);
            }
        }
    }

    private String getUsernameForSession(String sessionId) {
        synchronized (users) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                if (sessionId.equals(entry.getValue())) {
                    return entry.getKey(); // Return the username associated with the sessionId
                }
            }
        }
        return null; // Return null if no matching username is found
    }

    private void sendUserDisconnectMessage(String disconnectedUsername) {
        
        String message = disconnectedUsername + " has disconnected";
        eventQueue.offer(new WsEvent(EventType.USER_DISCONNECT, "Server", message));
       
    }

    @Scheduled(fixedRate = 300)
    public void processEvents() throws MessagingException, JsonProcessingException {
        
        while (!eventQueue.isEmpty()) {
            WsEvent event = eventQueue.poll();
            processEvent(event);
        }
    }

    private void processEvent(WsEvent event) throws MessagingException, JsonProcessingException {
        handleMessageEvent(event);
    }

    private void handleMessageEvent(WsEvent event) throws MessagingException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> message = new HashMap<>();
        message.put(event.getUsername(), event.getMessage());
        messagingTemplate.convertAndSend("/topic/messages", objectMapper.writeValueAsString(message));
    }
}