package com.example.mentorisebackend.repository.chat;


import com.example.mentorisebackend.api.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findByConversation_IdAndClientMessageId(Long conversationId, String clientMessageId);

    // הודעות אחרונות
    List<Message> findByConversation_IdOrderByIdDesc(Long conversationId, Pageable pageable);

    // pagination עם cursor (הבא "לפני" הודעה מסוימת)
    List<Message> findByConversation_IdAndIdLessThanOrderByIdDesc(Long conversationId, Long cursorId, Pageable pageable);
}