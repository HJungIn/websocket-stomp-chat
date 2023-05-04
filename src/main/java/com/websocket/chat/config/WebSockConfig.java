package com.websocket.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Configuration
@EnableWebSocketMessageBroker
public class WebSockConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/sub");
//                .setTaskScheduler(messageBrokerTaskScheduler1())
//                .setHeartbeatValue(new long[] {10000, 30000}); // client ping : 10s, server ping : 30s

        config.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp").setAllowedOrigins("*") // cors 허용을 위해 꼭 설정해주어야 함.
                .setHandshakeHandler(new DefaultHandshakeHandler() { //클라이언트 sessionId 가져오는 핸들러 설정

                    public boolean beforeHandshake(
                            ServerHttpRequest request,
                            ServerHttpResponse response,
                            WebSocketHandler wsHandler,
                            Map attributes) throws Exception {

                        if (request instanceof ServletServerHttpRequest) {
                            ServletServerHttpRequest servletRequest
                                    = (ServletServerHttpRequest) request;
                            HttpSession session = servletRequest
                                    .getServletRequest().getSession();
                            attributes.put("sessionId", session.getId());
                        }
                        return true;
                    }
                })
                .withSockJS()
                .setHeartbeatTime(10000);

    }


    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setSendTimeLimit(15 * 1000)
                .setSendBufferSizeLimit(512 * 1024)
                .setMessageSizeLimit(128 * 1024);

        registry.addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {  // 1. connect 되고 난 뒤
            @Override
            public WebSocketHandler decorate(WebSocketHandler handler) {
                return new WebSocketHandlerDecorator(handler) {
                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                        super.afterConnectionEstablished(session);
                        System.out.println("[ addDecoratorFactory ]Websocket session 1 = " + session);
//                        scheduleIdleTimeout(session);
                        session.getAttributes().put("lastLoginTime", System.currentTimeMillis());
                        session.getAttributes().put(session.getId(), session);
                        System.out.println("[ addDecoratorFactory ]Websocket session 2 = " + session.getAttributes());
                    }

                };
            }
        });

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) { // 2. 유저의 CONNECT와 DISCONNECT 할때의 정보 + send 할때도 들어옴
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                Map<String, Object> sessionAttributes = SimpMessageHeaderAccessor.getSessionAttributes(message.getHeaders());
                if (sessionAttributes != null) {
                    sessionAttributes.put("lastMessageTime", System.currentTimeMillis()); // 이걸로 덮어씌워짐
                }
                System.out.println("[configureClientInboundChannel]sessionAttributes = " + sessionAttributes);
                System.out.println("[configureClientInboundChannel]message = " + message);
                return message;
            }
        });
    }

    @Bean
    public TaskScheduler messageBrokerTaskScheduler1() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setThreadNamePrefix("message-broker-task-");
        taskScheduler.setPoolSize(1);
        return taskScheduler;

    }

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) { // 3. connect 되고 나서
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        System.out.println("headers.getSessionAttributes() = " + headers.getSessionAttributes());
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);
        Message<String> message = MessageBuilder.createMessage("", accessor.getMessageHeaders());

    }
}
