package com.ah.whatsapp.repository.entity;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ah.whatsapp.entity.ConversationEntity;

@Repository
public interface ConversationEntityRepository extends JpaRepository<ConversationEntity, UUID> {
  @Query(
      value =
          "select c.* from conversations c "
              + "join conversation_participants cp on c.id = cp.conversation_id "
              + "where cp.user_id = :userId order by c.updated_at desc",
      nativeQuery = true)
  List<ConversationEntity> findConversationsByUserId(UUID userId);
}
