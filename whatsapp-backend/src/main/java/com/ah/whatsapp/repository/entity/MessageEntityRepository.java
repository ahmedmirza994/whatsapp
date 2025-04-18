package com.ah.whatsapp.repository.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ah.whatsapp.entity.MessageEntity;

@Repository
public interface MessageEntityRepository extends JpaRepository<MessageEntity, UUID> {
  List<MessageEntity> findByConversationIdOrderBySentAtAsc(UUID conversationId);

  List<MessageEntity> findBySenderId(UUID senderId);
  Optional<MessageEntity> findByConversationIdOrderBySentAtDesc(UUID conversationId);

	/**
     * Finds the latest message entity for each conversation ID in the provided list
     * using a window function to rank messages within each conversation partition.
     *
     * @param conversationIds The list of conversation IDs.
     * @return A list of the latest MessageEntity objects for the given conversations.
     */
    @Query(
		value = """
				select m.* from ( select me.*, row_number() over(partition by me.conversation_id order by me.sent_at desc) as rn
				from messages me
				where me.conversation_id in (:conversationIds)
				) m
				where m.rn = 1
			""",
		nativeQuery = true
	)
    List<MessageEntity> findLatestMessagesForConversationIds(@Param("conversationIds") List<UUID> conversationIds);

}
