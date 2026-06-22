package com.example.mentorisebackend.configuration.websocket;

import com.example.mentorisebackend.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        StompCommand cmd = accessor.getCommand();
        log.debug("STOMP preSend command={}", cmd);

        try {
            if (StompCommand.CONNECT.equals(cmd)) {
                String authHeader = firstHeader(accessor, "Authorization");
                if (authHeader == null) authHeader = firstHeader(accessor, "authorization");

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new MessagingException("Missing Authorization: Bearer <token> on STOMP CONNECT");
                }

                String token = authHeader.substring(7);

                String username = normalizeEmail(jwtUtil.extractUsername(token));

                if (username == null || !jwtUtil.validateToken(token, username)) {
                    throw new MessagingException("Invalid JWT token");
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                accessor.setUser(auth);
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("STOMP CONNECT authenticated user={}", username);
            }

            if (accessor.getUser() instanceof Authentication auth) {
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            if (StompCommand.DISCONNECT.equals(cmd)) {
                SecurityContextHolder.clearContext();
            }

            return message;

        } catch (Exception e) {
            log.error("STOMP CONNECT failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String firstHeader(StompHeaderAccessor accessor, String name) {
        List<String> values = accessor.getNativeHeader(name);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}