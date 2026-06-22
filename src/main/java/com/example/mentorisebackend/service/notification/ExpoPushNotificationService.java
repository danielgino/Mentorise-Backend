package com.example.mentorisebackend.service.notification;

import com.example.mentorisebackend.api.entity.UserPushToken;
import com.example.mentorisebackend.repository.UserPushTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpoPushNotificationService {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private static final String DEVICE_NOT_REGISTERED = "DeviceNotRegistered";

    private final RestTemplate restTemplate;
    private final UserPushTokenRepository userPushTokenRepository;

    @Async
    @Transactional
    public void sendToUser(Long userId, String title, String body, Map<String, Object> data) {
        List<UserPushToken> tokens = userPushTokenRepository.findByUser_IdAndActiveTrue(userId);
        if (tokens.isEmpty()) {
            return;
        }

        List<Map<String, Object>> messages = new ArrayList<>();
        for (UserPushToken pushToken : tokens) {
            Map<String, Object> message = new HashMap<>();
            message.put("to", pushToken.getToken());
            message.put("title", title);
            message.put("body", body);
            message.put("data", data);
            message.put("sound", "default");
            messages.add(message);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("Accept-Encoding", "gzip, deflate");

            HttpEntity<List<Map<String, Object>>> httpRequest = new HttpEntity<>(messages, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    EXPO_PUSH_URL,
                    httpRequest,
                    Map.class
            );

            handleExpoResponse(response, tokens);

        } catch (Exception e) {
            log.warn("Failed to send Expo push notification to user {}: {}", userId, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void handleExpoResponse(Map<String, Object> response, List<UserPushToken> tokens) {
        if (response == null) return;

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("data");
        if (results == null) return;

        for (int i = 0; i < results.size() && i < tokens.size(); i++) {
            Map<String, Object> result = results.get(i);
            String status = (String) result.get("status");

            if ("error".equals(status)) {
                Map<String, Object> details = (Map<String, Object>) result.get("details");
                String errorCode = details != null ? (String) details.get("error") : null;

                if (DEVICE_NOT_REGISTERED.equals(errorCode)) {
                    UserPushToken staleToken = tokens.get(i);
                    staleToken.setActive(false);
                    userPushTokenRepository.save(staleToken);
                    log.info("Deactivated push token (DeviceNotRegistered) for user {}", staleToken.getUser().getId());
                } else {
                    log.warn("Push notification error at index {}: {} - {}", i, errorCode, result.get("message"));
                }
            }
        }
    }
}
