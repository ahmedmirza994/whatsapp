package com.ah.whatsapp.repository.entity;

import com.ah.whatsapp.entity.ConversationParticipantEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationParticipantEntityRepository
	extends JpaRepository<ConversationParticipantEntity, UUID> {

	@Query("select cpe from ConversationParticipantEntity cpe join fetch cpe.user where cpe.conversation.id = :conversationId")
	List<ConversationParticipantEntity> findByConversationIdAndIsActiveTrueWithUser(UUID conversationId);

	@Query("select cpe from ConversationParticipantEntity cpe join fetch cpe.user where cpe.conversation.id = :conversationId and cpe.user.id = :userId and cpe.isActive = true")
	Optional<ConversationParticipantEntity> findByConversationIdAndUserIdAndIsActiveTrue(
		UUID conversationId, UUID userId);

	boolean existsByConversationIdAndUserIdAndIsActiveTrue(UUID conversationId, UUID userId);

	@Query("select cpe from ConversationParticipantEntity cpe join fetch cpe.user where cpe.conversation.id in :conversationIds")
	List<ConversationParticipantEntity> findByConversationIdInAndIsActiveTrueWithUser(@Param("conversationIds") List<UUID> conversationIds);

	@Query("select cpe from ConversationParticipantEntity cpe join fetch cpe.user where cpe.conversation.id = :conversationId")
	List<ConversationParticipantEntity> findByConversationIdWithUser(UUID conversationId);

	@Query("select cpe from ConversationParticipantEntity cpe join fetch cpe.user where cpe.conversation.id = :conversationId and cpe.user.id = :userId")
	Optional<ConversationParticipantEntity> findByConversationIdAndUserId(
		UUID conversationId, UUID userId);
}
