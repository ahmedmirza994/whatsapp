/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.ah.whatsapp.exception.UnauthorizedException;
import com.ah.whatsapp.model.JwtUser;
import com.ah.whatsapp.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit Tests for JwtAuthenticationFilter
 * Tests core authentication functionality
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

	@Mock private JwtUtil jwtUtil;

	@Mock private UserDetailsService userDetailsService;

	@Mock private HttpServletRequest request;

	@Mock private HttpServletResponse response;

	@Mock private FilterChain filterChain;

	@Mock private SecurityContext securityContext;

	@Mock private JwtUser jwtUser;

	@InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

	private String validToken;
	private String testEmail;

	@BeforeEach
	void setUp() {
		validToken = "valid.jwt.token";
		testEmail = "test@example.com";
	}

	@Test
	@DisplayName("Should authenticate user with valid Bearer token")
	void shouldAuthenticateUserWithValidBearerToken() throws ServletException, IOException {
		// Given
		String authorizationHeader = "Bearer " + validToken;
		when(request.getHeader("Authorization")).thenReturn(authorizationHeader);
		when(jwtUtil.validateToken(validToken)).thenReturn(true);
		when(jwtUtil.extractEmail(validToken)).thenReturn(testEmail);
		when(userDetailsService.loadUserByUsername(testEmail)).thenReturn(jwtUser);

		try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
				Mockito.mockStatic(SecurityContextHolder.class)) {

			mockedSecurityContextHolder
					.when(SecurityContextHolder::getContext)
					.thenReturn(securityContext);
			when(securityContext.getAuthentication()).thenReturn(null);

			// When
			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

			// Then
			ArgumentCaptor<UsernamePasswordAuthenticationToken> authTokenCaptor =
					ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
			verify(securityContext).setAuthentication(authTokenCaptor.capture());

			UsernamePasswordAuthenticationToken capturedToken = authTokenCaptor.getValue();
			assertEquals(jwtUser, capturedToken.getPrincipal());
			assertNull(capturedToken.getCredentials());

			verify(filterChain).doFilter(request, response);
			verify(jwtUtil).validateToken(validToken);
			verify(jwtUtil).extractEmail(validToken);
			verify(userDetailsService).loadUserByUsername(testEmail);
		}
	}

	@Test
	@DisplayName("Should skip authentication when user already authenticated")
	void shouldSkipAuthenticationWhenUserAlreadyAuthenticated()
			throws ServletException, IOException {
		// Given
		String authorizationHeader = "Bearer " + validToken;
		when(request.getHeader("Authorization")).thenReturn(authorizationHeader);
		when(jwtUtil.validateToken(validToken)).thenReturn(true);
		when(jwtUtil.extractEmail(validToken)).thenReturn(testEmail);

		UsernamePasswordAuthenticationToken existingAuth =
				new UsernamePasswordAuthenticationToken(
						jwtUser, null, java.util.Collections.emptyList());

		try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
				Mockito.mockStatic(SecurityContextHolder.class)) {

			mockedSecurityContextHolder
					.when(SecurityContextHolder::getContext)
					.thenReturn(securityContext);
			when(securityContext.getAuthentication()).thenReturn(existingAuth);

			// When
			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

			// Then
			verify(userDetailsService, never()).loadUserByUsername(anyString());
			verify(securityContext, never()).setAuthentication(any());
			verify(filterChain).doFilter(request, response);
		}
	}

	@Test
	@DisplayName("Should throw UnauthorizedException for invalid token")
	void shouldThrowUnauthorizedExceptionForInvalidToken() throws ServletException, IOException {
		// Given
		String invalidToken = "invalid.token";
		String authorizationHeader = "Bearer " + invalidToken;
		when(request.getHeader("Authorization")).thenReturn(authorizationHeader);
		when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

		try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
				Mockito.mockStatic(SecurityContextHolder.class)) {

			// When & Then
			UnauthorizedException exception =
					assertThrows(
							UnauthorizedException.class,
							() ->
									jwtAuthenticationFilter.doFilterInternal(
											request, response, filterChain));

			assertEquals("Invalid token", exception.getMessage());

			// Verify security context was cleared
			mockedSecurityContextHolder.verify(SecurityContextHolder::clearContext);
			verify(filterChain, never()).doFilter(request, response);
		}
	}

	@Test
	@DisplayName("Should throw UnauthorizedException when token validation throws exception")
	void shouldThrowUnauthorizedExceptionWhenTokenValidationThrowsException()
			throws ServletException, IOException {
		// Given
		String invalidToken = "malformed.token";
		String authorizationHeader = "Bearer " + invalidToken;
		when(request.getHeader("Authorization")).thenReturn(authorizationHeader);
		when(jwtUtil.validateToken(invalidToken))
				.thenThrow(new RuntimeException("Token parsing error"));

		try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
				Mockito.mockStatic(SecurityContextHolder.class)) {

			// When & Then
			UnauthorizedException exception =
					assertThrows(
							UnauthorizedException.class,
							() ->
									jwtAuthenticationFilter.doFilterInternal(
											request, response, filterChain));

			assertEquals("Invalid token", exception.getMessage());

			// Verify security context was cleared
			mockedSecurityContextHolder.verify(SecurityContextHolder::clearContext);
			verify(filterChain, never()).doFilter(request, response);
		}
	}

	@Test
	@DisplayName("Should process request normally when no Authorization header")
	void shouldProcessRequestNormallyWhenNoAuthorizationHeader()
			throws ServletException, IOException {
		// Given
		when(request.getHeader("Authorization")).thenReturn(null);

		// When
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// Then
		verify(jwtUtil, never()).validateToken(anyString());
		verify(jwtUtil, never()).extractEmail(anyString());
		verify(userDetailsService, never()).loadUserByUsername(anyString());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName(
			"Should process request normally when Authorization header doesn't start with Bearer")
	void shouldProcessRequestNormallyWhenAuthorizationHeaderDoesNotStartWithBearer()
			throws ServletException, IOException {
		// Given
		when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

		// When
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// Then
		verify(jwtUtil, never()).validateToken(anyString());
		verify(jwtUtil, never()).extractEmail(anyString());
		verify(userDetailsService, never()).loadUserByUsername(anyString());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Should handle empty Bearer token")
	void shouldHandleEmptyBearerToken() throws ServletException, IOException {
		// Given
		when(request.getHeader("Authorization")).thenReturn("Bearer ");

		// When
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// Then
		verify(jwtUtil, never()).validateToken(anyString());
		verify(jwtUtil, never()).extractEmail(anyString());
		verify(userDetailsService, never()).loadUserByUsername(anyString());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Should handle user not found from UserDetailsService")
	void shouldHandleUserNotFoundFromUserDetailsService() throws ServletException, IOException {
		// Given
		String authorizationHeader = "Bearer " + validToken;
		when(request.getHeader("Authorization")).thenReturn(authorizationHeader);
		when(jwtUtil.validateToken(validToken)).thenReturn(true);
		when(jwtUtil.extractEmail(validToken)).thenReturn(testEmail);
		when(userDetailsService.loadUserByUsername(testEmail)).thenReturn(null);

		try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
				Mockito.mockStatic(SecurityContextHolder.class)) {

			mockedSecurityContextHolder
					.when(SecurityContextHolder::getContext)
					.thenReturn(securityContext);
			when(securityContext.getAuthentication()).thenReturn(null);

			// When
			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

			// Then
			verify(securityContext, never()).setAuthentication(any());
			verify(filterChain).doFilter(request, response);
		}
	}
}
