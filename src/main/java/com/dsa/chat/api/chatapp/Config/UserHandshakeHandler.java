package com.dsa.chat.api.chatapp.Config;

import com.sun.security.auth.UserPrincipal;

import java.security.Principal;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
public class UserHandshakeHandler extends DefaultHandshakeHandler {

    private final Logger logger = LoggerFactory.getLogger(UserHandshakeHandler.class);

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String sessionId = extractSessionId(request.getURI().toString());

        if (sessionId != null) {
            // Log the session ID
            logger.info("User connected with session ID: " + sessionId);
            return new UserPrincipal(sessionId);
        } else {
            return null; // Handle the case where the session ID is not found
        }
    }
    
    private String extractSessionId(String uri) {
        Pattern pattern = Pattern.compile("/ws/[^/]+/([^/]+)/websocket");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
}
