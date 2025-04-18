package com.ah.whatsapp.repository;

import java.util.List;
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
                // Load participants and last message when finding by ID as well
                loadParticipantsAndLastMessage(conversation);
                return conversation;
            });
	}

	@Override
	public List<Conversation> findByUserId(UUID userId) {
		List<ConversationEntity> conversationEntities = conversationEntityRepository.findConversationsByUserId(userId);

        return conversationEntities.stream()
            .map(entity -> {
                Conversation conversation = conversationMapper.toModel(entity);
                // Load participants and last message for each conversation
                loadParticipantsAndLastMessage(conversation);
                return conversation;
            })
            .collect(Collectors.toList());
	}

	private void loadParticipantsAndLastMessage(Conversation conversation) {
        // Load participants
        List<ConversationParticipant> participants = conversationParticipantRepository.findByConversationId(conversation.getId());
        conversation.setParticipants(participants);

        // Load only the last message
        Optional<Message> lastMessageOpt = messageRepository.findLatestByConversationId(conversation.getId());
        lastMessageOpt.ifPresent(conversation::setLastMessage); // Use method reference
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
