package com.ah.whatsapp.repository.entity;

import com.ah.whatsapp.entity.MessageEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageEntityRepository extends JpaRepository<MessageEntity, UUID> {
  List<MessageEntity> findByConversationIdOrderBySentAtAsc(UUID conversationId);

  List<MessageEntity> findBySenderId(UUID senderId);
}
