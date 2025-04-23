package com.ah.whatsapp.repository.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.mapper.ConversationMapper;
import com.ah.whatsapp.model.Conversation;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.model.Message;
import com.ah.whatsapp.repository.ConversationParticipantRepository;
import com.ah.whatsapp.repository.ConversationRepository;
import com.ah.whatsapp.repository.MessageRepository;
import com.ah.whatsapp.repository.entity.ConversationEntityRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepository {
	private final ConversationEntityRepository conversationEntityRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final MessageRepository messageRepository;
    private final ConversationMapper conversationMapper;


	@Override
	@Transactional
	public Conversation save(Conversation conversation) {
		ConversationEntity entity = conversationMapper.toEntity(conversation);
        ConversationEntity savedEntity = conversationEntityRepository.save(entity);
        return conversationMapper.toModel(savedEntity);
	}

	@Override
	public Optional<Conversation> findById(UUID id) {
		return conversationEntityRepository.findById(id)
            .map(entity -> {
                Conversation conversation = conversationMapper.toModel(entity);
	            loadLastMessage(conversation);
				loadParticipants(conversation);
                return conversation;
            });
	}

	@Override
	public List<Conversation> findByUserId(UUID userId) {
		List<ConversationEntity> conversationEntities = conversationEntityRepository.findConversationsByUserId(userId);

		List<UUID> conversationIds = conversationEntities.stream()
                .map(ConversationEntity::getId)
                .collect(Collectors.toList());

		Map<UUID, Message> lastMessagesMap = messageRepository.findLatestMessagesForConversations(conversationIds);
		Map<UUID, List<ConversationParticipant>> participantsMap = conversationParticipantRepository.findParticipantsForConversations(conversationIds);

        return conversationEntities.stream()
            .map(entity -> {
                Conversation conversation = conversationMapper.toModel(entity);
	            conversation.setLastMessage(lastMessagesMap.get(entity.getId()));
				conversation.setParticipants(participantsMap.getOrDefault(entity.getId(), Collections.emptyList()));
                return conversation;
            })
            .collect(Collectors.toList());
	}

	private void loadLastMessage(Conversation conversation) {
        Optional<Message> lastMessageOpt = messageRepository.findLatestByConversationId(conversation.getId());
        lastMessageOpt.ifPresent(conversation::setLastMessage);
    }

	private void loadParticipants(Conversation conversation) {
        List<ConversationParticipant> participants =
			conversationParticipantRepository.findByConversationId(conversation.getId());
        conversation.setParticipants(participants);
    }

	@Override
	@Transactional
	public void delete(UUID id) {
		conversationEntityRepository.deleteById(id);
	}

	@Override
	public boolean existsById(UUID id) {
		 return conversationEntityRepository.existsById(id);
	}

}
