/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.configuration;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.ah.whatsapp.model.JwtUser;
import com.ah.whatsapp.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthChannelInterceptor implements ChannelInterceptor {

	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService; // Your UserDetailsServiceImpl

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor =
				MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
			// Extract token from STOMP connect headers
			// Note: StompHeaderAccessor might automatically convert header names to lowercase
			// Check both 'Authorization' and 'authorization'
			List<String> authorization = accessor.getNativeHeader("Authorization");
			if (authorization == null) {
				authorization = accessor.getNativeHeader("authorization");
			}

			String jwt = null;
			if (authorization != null && !authorization.isEmpty()) {
				String authHeader = authorization.get(0);
				if (authHeader != null && authHeader.startsWith("Bearer ")) {
					jwt = authHeader.substring(7);
				}
			}

			if (jwt != null) {
				try {
					if (jwtUtil.validateToken(jwt)) {
						String username = jwtUtil.extractEmail(jwt);
						if (username != null) {
							JwtUser userDetails =
									(JwtUser) userDetailsService.loadUserByUsername(username);
							// Create authentication token
							UsernamePasswordAuthenticationToken authentication =
									new UsernamePasswordAuthenticationToken(
											userDetails,
											null,
											userDetails.getAuthorities()); // Use authorities from
							// UserDetails

							// Set the user for the STOMP session/message
							// This is crucial for @AuthenticationPrincipal to work later

							log.info(
									"Setting WebSocket Principal for user: '{}', Principal Name:"
											+ " '{}'",
									username,
									authentication.getName());
							accessor.setUser(authentication);
							log.info("Authenticated WebSocket user: {}", username);
						}
					}
				} catch (Exception e) {
					log.error("WebSocket authentication error: {}", e.getMessage());
					// Optionally prevent connection by throwing an exception or returning null
					// For now, just log the error
				}
			} else {
				log.warn("WebSocket CONNECT message without valid Authorization header.");
				// Decide if unauthenticated connections are allowed or should be rejected
			}
		}

		return message;
	}
}
