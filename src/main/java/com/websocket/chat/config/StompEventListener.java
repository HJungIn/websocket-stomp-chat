package com.websocket.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class StompEventListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private Set<String> sessionSet = new HashSet<>();
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) throws IOException {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
sessionSet.add(sessionId);

//        WebSocketSession session = (WebSocketSession) event.getSource();
//        session.setMaxIdleTimeout(Duration.ofMinutes(5));

//        session.sendMessage(new PingMessage());
//        session.sendMessage(new PingMessage());
//        session.getAttributes().put("heart-beat", new int[]{8000, 8000});

        log.info("[Connected] websocket session id : {}, count : {}", sessionId, sessionSet.size());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
sessionSet.remove(sessionId);


        System.out.println("event = " + event);
        System.out.println("headerAccessor = " + headerAccessor);
//        messagingTemplate.convertAndSend("/sub/disconnect-session", sessionId);
        log.info("[Disconnected] websocket session id : {}, count : {}", sessionId, sessionSet.size());
    }


}
