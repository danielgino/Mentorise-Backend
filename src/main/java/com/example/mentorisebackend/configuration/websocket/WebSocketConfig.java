package com.example.mentorisebackend.configuration.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtStompChannelInterceptor jwtStompChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // DEV: אפשר "*". בפרודקשן שים דומיינים ספציפיים
                .setAllowedOriginPatterns("*");
        // אם תרצה SockJS:
        // .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // הלקוח ישלח לשרת ליעדים שמתחילים ב /app
        registry.setApplicationDestinationPrefixes("/app");

        // broker פנימי למינימום (MVP)
        registry.enableSimpleBroker("/topic", "/queue");

        // הודעות "למשתמש ספציפי": /user/queue/...
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // פה אנחנו "תופסים" CONNECT/SEND ומאמתים JWT
        registration.interceptors(jwtStompChannelInterceptor);
    }
}