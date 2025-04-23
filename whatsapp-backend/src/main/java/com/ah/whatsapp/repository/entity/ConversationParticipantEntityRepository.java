package com.ah.whatsapp.repository.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ah.whatsapp.entity.ConversationParticipantEntity;

@Repository
public interface ConversationParticipantEntityRepository
    extends JpaRepository<ConversationParticipantEntity, UUID> {
  List<ConversationParticipantEntity> findByConversationId(UUID conversationId);

  List<ConversationParticipantEntity> findByUserId(UUID userId);

  Optional<ConversationParticipantEntity> findByConversationIdAndUserId(
      UUID conversationId, UUID userId);

  boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

  List<ConversationParticipantEntity> findByConversationIdIn(List<UUID> conversationIds);

  @Query("select cpe from ConversationParticipantEntity cpe join fetch cpe.user where cpe.conversation.id in :conversationIds")
    List<ConversationParticipantEntity> findByConversationIdInWithUser(@Param("conversationIds") List<UUID> conversationIds);
}
