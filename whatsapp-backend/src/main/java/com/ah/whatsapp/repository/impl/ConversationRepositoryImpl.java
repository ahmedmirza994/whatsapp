/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        return conversationEntityRepository
                .findById(id)
                .map(
                        entity -> {
                            Conversation conversation = conversationMapper.toModel(entity);
                            loadLastMessage(conversation);
                            loadParticipants(conversation);
                            return conversation;
                        });
    }

    @Override
    public List<Conversation> findByUserId(UUID userId) {
        List<ConversationEntity> conversationEntities =
                conversationEntityRepository.findConversationsByUserId(userId);

        List<UUID> conversationIds =
                conversationEntities.stream().map(ConversationEntity::getId).toList();

        Map<UUID, Message> lastMessagesMap =
                messageRepository.findLatestMessagesForConversations(conversationIds);
        Map<UUID, List<ConversationParticipant>> participantsMap =
                conversationParticipantRepository.findParticipantsForConversationsAndIsActiveTrue(
                        conversationIds);

        Map<UUID, ConversationParticipant> userParticipantMap =
                participantsMap.entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        e ->
                                                e.getValue().stream()
                                                        .filter(
                                                                p ->
                                                                        p.getParticipantId()
                                                                                .equals(userId))
                                                        .findFirst()
                                                        .orElse(null)));

        return conversationEntities.stream()
                .map(
                        entity -> {
                            Conversation conversation = conversationMapper.toModel(entity);
                            conversation.setParticipants(
                                    participantsMap.getOrDefault(
                                            entity.getId(), Collections.emptyList()));

                            ConversationParticipant userParticipant =
                                    userParticipantMap.get(entity.getId());
                            conversation.setLastMessage(lastMessagesMap.get(entity.getId()));

                            if (userParticipant != null
                                    && conversation.getLastMessage() != null
                                    && conversation
                                            .getLastMessage()
                                            .getSentAt()
                                            .isBefore(userParticipant.getJoinedAt())) {
                                conversation.setLastMessage(null);
                            }

                            return conversation;
                        })
                .toList();
    }

    private void loadLastMessage(Conversation conversation) {
        Optional<Message> lastMessageOpt =
                messageRepository.findLatestByConversationId(conversation.getId());
        lastMessageOpt.ifPresent(conversation::setLastMessage);
    }

    private void loadParticipants(Conversation conversation) {
        List<ConversationParticipant> participants =
                conversationParticipantRepository.findByConversationIdAndIsActiveTrue(
                        conversation.getId());
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

    @Override
    public Optional<Conversation> findDirectConversationBetweenUsers(UUID userId1, UUID userId2) {
        log.debug("Searching for direct conversation between users {} and {}", userId1, userId2);
        // Find conversations where BOTH users are participants
        List<UUID> potentialConversationIds =
                conversationEntityRepository.findConversationsWithParticipants(
                        List.of(userId1, userId2), 2L); // Ensure exactly 2 participants

        if (potentialConversationIds.isEmpty()) {
            log.debug(
                    "No potential direct conversations found between {} and {}", userId1, userId2);
            return Optional.empty();
        }

        // Since the query ensures exactly 2 participants and both are present,
        // any result is a direct conversation. We take the first one found.
        // We still need to load participants and last message for the found conversation.
        UUID conversationId = potentialConversationIds.get(0);
        log.info(
                "Found direct conversation {} between users {} and {}",
                conversationId,
                userId1,
                userId2);
        return findById(conversationId); // Reuse findById to load details
    }
}
