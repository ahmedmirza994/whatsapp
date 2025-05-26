/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ah.whatsapp.dto.ApiResponse;
import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.CreateConversationRequest;
import com.ah.whatsapp.model.JwtUser;
import com.ah.whatsapp.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * GET /api/conversations : Get all conversations for the authenticated user.
     *
     * @param jwtUser The authenticated user principal.
     * @return A list of ConversationDto objects.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationDto>>> getUserConversations(
            @AuthenticationPrincipal JwtUser jwtUser) {
        if (jwtUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<ConversationDto> conversations =
                conversationService.findUserConversations(jwtUser.getUserId());
        return new ResponseEntity<>(ApiResponse.success(conversations), HttpStatus.OK);
    }

    /**
     * POST /api/conversations : Create a new conversation with another user.
     *
     * @param request  The request containing the participant ID.
     * @param jwtUser The authenticated user principal (creator).
     * @return The created ConversationDto.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ConversationDto>> createConversation(
            @Valid @RequestBody CreateConversationRequest request,
            @AuthenticationPrincipal JwtUser jwtUser) {
        if (jwtUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ConversationDto conversation =
                conversationService.createConversation(request, jwtUser.getUserId());
        return new ResponseEntity<>(ApiResponse.success(conversation), HttpStatus.CREATED);
    }

    /**
     * GET /conversations/{id} : Get a specific conversation by ID for the authenticated user.
     *
     * @param id      The ID of the conversation.
     * @param jwtUser The authenticated user principal.
     * @return The ConversationDto if found and user is a participant.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConversationDto>> getConversationById(
            @PathVariable(name = "id") UUID id, @AuthenticationPrincipal JwtUser jwtUser) {
        if (jwtUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Service method already handles ConversationNotFoundException and AccessDeniedException
        ConversationDto conversation =
                conversationService.findConversationByIdAndUser(id, jwtUser.getUserId());
        return new ResponseEntity<>(ApiResponse.success(conversation), HttpStatus.OK);
    }

    @PostMapping("/find-or-create")
    public ResponseEntity<ApiResponse<ConversationDto>> findOrCreateDirectConversation(
            @RequestBody
                    CreateConversationRequest request, // Reuse request DTO to get participantId
            @AuthenticationPrincipal JwtUser currentUser) {
        log.info(
                "Received request to find or create direct conversation between user: {} and"
                        + " participant: {}",
                currentUser.getUserId(),
                request.participantId());
        ConversationDto conversation =
                conversationService.findOrCreateConversation(request, currentUser.getUserId());
        log.info(
                "Returning conversation {} after find-or-create for user: {}",
                conversation.getId(),
                currentUser.getUserId());
        return new ResponseEntity<>(ApiResponse.success(conversation), HttpStatus.CREATED);
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<UUID>> deleteConversation(
            @PathVariable(name = "conversationId") UUID conversationId, @AuthenticationPrincipal JwtUser currentUser) {
        conversationService.deleteConversationForUser(conversationId, currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(conversationId));
    }

    @PostMapping("/{conversationId}/read")
    public ResponseEntity<ApiResponse<UUID>> markAsRead(
            @PathVariable(name = "conversationId") UUID conversationId, @AuthenticationPrincipal JwtUser currentUser) {
        conversationService.markConversationAsRead(conversationId, currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(conversationId));
    }
}
