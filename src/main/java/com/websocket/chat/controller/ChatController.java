package com.websocket.chat.controller;

import com.websocket.chat.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Controller;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final SimpMessagingTemplate template;

    @MessageMapping("/chat/message")
//    @SendTo(value = "/sub/chat/room/{message.id}")
    public void message(ChatMessage message) {

//        // Set the message to expire in 1 seconds => 메세지 만료
        long expirationTime = System.currentTimeMillis() + (100 * 1000);
        Map<String, Object> headers = new HashMap<>();
        headers.put("expires", expirationTime);
//        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message, headers);


        if (ChatMessage.MessageType.ENTER.equals(message.getType()))
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        if (ChatMessage.MessageType.LEAVE.equals(message.getType()))
            message.setMessage(message.getSender() + "님이 퇴장하셨습니다.");


//        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
//        accessor.setHeader("stomp-expiration", System.currentTimeMillis() + 5000); // 5초
//        template.convertAndSend();

        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message, headers);
    }

}
