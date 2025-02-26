package com.ah.whatsapp.repository.entity;

import com.ah.whatsapp.entity.ConversationParticipantEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationParticipantEntityRepository
    extends JpaRepository<ConversationParticipantEntity, UUID> {
  List<ConversationParticipantEntity> findByConversationId(UUID conversationId);

  List<ConversationParticipantEntity> findByUserId(UUID userId);

  Optional<ConversationParticipantEntity> findByConversationIdAndUserId(
      UUID conversationId, UUID userId);

  boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);
}
