package com.ah.whatsapp.controller;

import com.ah.whatsapp.dto.ApiResponse;
import com.ah.whatsapp.model.JwtUser;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;

import org.springframework.http.HttpStatus; // Added
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.CreateConversationRequest;
import com.ah.whatsapp.service.ConversationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
        List<ConversationDto> conversations = conversationService.findUserConversations(jwtUser.getUserId());
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
        ConversationDto conversation = conversationService.createConversation(request, jwtUser.getUserId());
        return new ResponseEntity<>(ApiResponse.success(conversation), HttpStatus.CREATED);
    }

}
