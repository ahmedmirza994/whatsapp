/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ah.whatsapp.constant.WebSocketConstants;
import com.ah.whatsapp.dto.ApiResponse;
import com.ah.whatsapp.dto.MessageDto;
import com.ah.whatsapp.dto.SendMessageRequest;
import com.ah.whatsapp.model.JwtUser;
import com.ah.whatsapp.service.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/messages") // Base path for message-related endpoints
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * GET /messages/conversation/{conversationId} : Get messages for a specific conversation.
     *
     * @param conversationId The ID of the conversation.
     * @param jwtUser        The authenticated user principal (for authorization checks if needed).
     * @return A list of MessageDto objects for the conversation, wrapped in ApiResponse.
     */
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getConversationMessages(
            @PathVariable UUID conversationId, @AuthenticationPrincipal JwtUser jwtUser) {
        if (jwtUser == null) {
            // Handled by security config, but good practice for clarity
            return new ResponseEntity<>(
                    ApiResponse.failure("Unauthorized", HttpStatus.UNAUTHORIZED),
                    HttpStatus.UNAUTHORIZED);
        }
        List<MessageDto> messages =
                messageService.findConversationMessages(conversationId, jwtUser.getUserId());
        return new ResponseEntity<>(ApiResponse.success(messages), HttpStatus.OK);
    }

    /**
     * Handles incoming chat messages sent via WebSocket.
     * Destination: /app/chat.sendMessage
     *
     * @param request The message payload containing conversationId and content.
     * @param accessor The STOMP header accessor containing the user principal set by the interceptor.
     */
    @MessageMapping(WebSocketConstants.CHAT_SEND_MESSAGE)
    public void handleSendMessage(
            @Payload @Valid SendMessageRequest request,
            StompHeaderAccessor accessor) { // Inject StompHeaderAccessor

        Principal userPrincipal = accessor.getUser(); // Get the Principal set by the interceptor

        if (userPrincipal instanceof UsernamePasswordAuthenticationToken authToken
                && authToken.getPrincipal() instanceof JwtUser jwtUser) {

            // Now 'jwtUser' is the fully populated object from the interceptor
            if (jwtUser.getUserId() == null) {
                log.error(
                        "WebSocket user principal (JwtUser) has null fields despite being"
                                + " authenticated. Principal: {}",
                        jwtUser);
                // Handle error appropriately - maybe send error back to user?
                return;
            }

            log.info(
                    "Received message via WebSocket from user {}: {}",
                    jwtUser.getUserId(),
                    request);
            messageService.sendMessage(request, jwtUser.getUserId());

        } else {
            // This should not happen if AuthChannelInterceptor is working correctly
            log.error(
                    "Unauthenticated or unexpected principal type for WebSocket message. Principal:"
                            + " {}",
                    userPrincipal);
        }
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<UUID>> deleteMessage(
            @PathVariable UUID messageId, @AuthenticationPrincipal JwtUser jwtUser) {
        if (jwtUser == null) {
            return new ResponseEntity<>(
                    ApiResponse.failure("Unauthorized", HttpStatus.UNAUTHORIZED),
                    HttpStatus.UNAUTHORIZED);
        }
        messageService.deleteMessage(messageId, jwtUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(messageId));
    }
}
