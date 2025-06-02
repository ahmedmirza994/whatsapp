/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil Tests")
class JwtUtilTest {

	private JwtUtil jwtUtil;
	private static final String TEST_SECRET =
			"dGVzdC1zZWNyZXQtdGhhdC1pcy1hdC1sZWFzdC0yNTYtYml0cy1sb25nLWZvci1obWFjLXNoYTI1Ng==";
	private static final Long TEST_EXPIRATION = 3600L; // 1 hour in seconds
	private static final String TEST_EMAIL = "test@example.com";

	@BeforeEach
	void setUp() throws Exception {
		jwtUtil = new JwtUtil();
		setPrivateField(jwtUtil, "secret", TEST_SECRET);
		setPrivateField(jwtUtil, "expiration", TEST_EXPIRATION);
	}

	@Nested
	@DisplayName("Token Generation Tests")
	class TokenGenerationTests {

		@Test
		@DisplayName("Should generate valid JWT token with email")
		void shouldGenerateValidJwtTokenWithEmail() {
			// When
			String token = jwtUtil.generateToken(TEST_EMAIL);

			// Then
			assertNotNull(token);
			assertFalse(token.isEmpty());
			assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots

			// Verify token structure
			Claims claims =
					Jwts.parser()
							.verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET)))
							.build()
							.parseSignedClaims(token)
							.getPayload();

			assertEquals(TEST_EMAIL, claims.getSubject());
			assertNotNull(claims.getId()); // UUID should be present
			assertNotNull(claims.getIssuedAt());
			assertNotNull(claims.getExpiration());
		}

		@Test
		@DisplayName("Should generate different tokens for same email")
		void shouldGenerateDifferentTokensForSameEmail() {
			// When
			String token1 = jwtUtil.generateToken(TEST_EMAIL);
			String token2 = jwtUtil.generateToken(TEST_EMAIL);

			// Then
			assertNotEquals(token1, token2);
		}

		@Test
		@DisplayName("Should generate token with correct expiration time")
		void shouldGenerateTokenWithCorrectExpirationTime() {
			// Given
			long beforeGeneration = System.currentTimeMillis();

			// When
			String token = jwtUtil.generateToken(TEST_EMAIL);

			// Then
			long afterGeneration = System.currentTimeMillis();
			Date expiration = jwtUtil.extractExpiration(token);

			// Check that expiration is approximately correct (within 1 second tolerance)
			long expectedExpiration = beforeGeneration + (TEST_EXPIRATION * 1000);
			long actualExpiration = expiration.getTime();

			assertTrue(actualExpiration >= expectedExpiration - 1000);
			assertTrue(actualExpiration <= afterGeneration + (TEST_EXPIRATION * 1000));
		}

		@Test
		@DisplayName("Should handle null email without throwing exception")
		void shouldHandleNullEmailWithoutThrowingException() {
			// When & Then
			assertDoesNotThrow(
					() -> {
						String token = jwtUtil.generateToken(null);
						assertNotNull(token);
					});
		}

		@Test
		@DisplayName("Should handle empty email without throwing exception")
		void shouldHandleEmptyEmailWithoutThrowingException() {
			// When & Then
			assertDoesNotThrow(
					() -> {
						String token = jwtUtil.generateToken("");
						assertNotNull(token);
					});
		}
	}

	@Nested
	@DisplayName("Token Validation Tests")
	class TokenValidationTests {

		@Test
		@DisplayName("Should validate valid token")
		void shouldValidateValidToken() {
			// Given
			String token = jwtUtil.generateToken(TEST_EMAIL);

			// When
			Boolean isValid = jwtUtil.validateToken(token);

			// Then
			assertTrue(isValid);
		}

		@Test
		@DisplayName("Should not validate malformed token")
		void shouldNotValidateMalformedToken() {
			// Given
			String malformedToken = "invalid.token.here";

			// When
			Boolean isValid = jwtUtil.validateToken(malformedToken);

			// Then
			assertFalse(isValid);
		}

		@Test
		@DisplayName("Should not validate token with wrong signature")
		void shouldNotValidateTokenWithWrongSignature() {
			// Given
			String tokenWithWrongSignature =
					Jwts.builder()
							.subject(TEST_EMAIL)
							.issuedAt(new Date())
							.expiration(new Date(System.currentTimeMillis() + 3600000))
							.signWith(
									Keys.hmacShaKeyFor(
											"different-secret-key-for-testing-wrong-signature"
													.getBytes()))
							.compact();

			// When
			Boolean isValid = jwtUtil.validateToken(tokenWithWrongSignature);

			// Then
			assertFalse(isValid);
		}

		@Test
		@DisplayName("Should not validate expired token")
		void shouldNotValidateExpiredToken() throws Exception {
			// Given - create a JwtUtil with very short expiration
			JwtUtil shortExpirationJwtUtil = new JwtUtil();
			setPrivateField(shortExpirationJwtUtil, "secret", TEST_SECRET);
			setPrivateField(shortExpirationJwtUtil, "expiration", -1L); // Already expired

			String expiredToken = shortExpirationJwtUtil.generateToken(TEST_EMAIL);

			// When
			Boolean isValid = jwtUtil.validateToken(expiredToken);

			// Then
			assertFalse(isValid);
		}

		@Test
		@DisplayName("Should not validate null token")
		void shouldNotValidateNullToken() {
			// When
			Boolean isValid = jwtUtil.validateToken(null);

			// Then
			assertFalse(isValid);
		}

		@Test
		@DisplayName("Should not validate empty token")
		void shouldNotValidateEmptyToken() {
			// When
			Boolean isValid = jwtUtil.validateToken("");

			// Then
			assertFalse(isValid);
		}
	}

	@Nested
	@DisplayName("Email Extraction Tests")
	class EmailExtractionTests {

		@Test
		@DisplayName("Should extract correct email from valid token")
		void shouldExtractCorrectEmailFromValidToken() {
			// Given
			String token = jwtUtil.generateToken(TEST_EMAIL);

			// When
			String extractedEmail = jwtUtil.extractEmail(token);

			// Then
			assertEquals(TEST_EMAIL, extractedEmail);
		}

		@Test
		@DisplayName("Should extract email for different test emails")
		void shouldExtractEmailForDifferentTestEmails() {
			// Given
			String[] testEmails = {
				"user1@example.com", "admin@company.org", "test.user+tag@domain.co.uk"
			};

			for (String email : testEmails) {
				// When
				String token = jwtUtil.generateToken(email);
				String extractedEmail = jwtUtil.extractEmail(token);

				// Then
				assertEquals(email, extractedEmail);
			}
		}

		@Test
		@DisplayName("Should throw exception when extracting email from invalid token")
		void shouldThrowExceptionWhenExtractingEmailFromInvalidToken() {
			// Given
			String invalidToken = "invalid.token.here";

			// When & Then
			Exception exception =
					assertThrows(
							MalformedJwtException.class, () -> jwtUtil.extractEmail(invalidToken));
			assertNotNull(exception);
		}
	}

	@Nested
	@DisplayName("Expiration Extraction Tests")
	class ExpirationExtractionTests {

		@Test
		@DisplayName("Should extract correct expiration from valid token")
		void shouldExtractCorrectExpirationFromValidToken() {
			// Given
			long beforeGeneration = System.currentTimeMillis();
			String token = jwtUtil.generateToken(TEST_EMAIL);
			long afterGeneration = System.currentTimeMillis();

			// When
			Date expiration = jwtUtil.extractExpiration(token);

			// Then
			assertNotNull(expiration);
			long expectedExpiration = beforeGeneration + (TEST_EXPIRATION * 1000);
			assertTrue(expiration.getTime() >= expectedExpiration - 1000);
			assertTrue(expiration.getTime() <= afterGeneration + (TEST_EXPIRATION * 1000));
		}

		@Test
		@DisplayName("Should throw exception when extracting expiration from invalid token")
		void shouldThrowExceptionWhenExtractingExpirationFromInvalidToken() {
			// Given
			String invalidToken = "invalid.token.here";

			// When & Then
			Exception exception =
					assertThrows(
							MalformedJwtException.class,
							() -> jwtUtil.extractExpiration(invalidToken));
			assertNotNull(exception);
		}
	}

	@Nested
	@DisplayName("Generic Claim Extraction Tests")
	class GenericClaimExtractionTests {

		@Test
		@DisplayName("Should extract subject claim using generic method")
		void shouldExtractSubjectClaimUsingGenericMethod() {
			// Given
			String token = jwtUtil.generateToken(TEST_EMAIL);

			// When
			String subject = jwtUtil.extractClaim(token, Claims::getSubject);

			// Then
			assertEquals(TEST_EMAIL, subject);
		}

		@Test
		@DisplayName("Should extract issued at claim using generic method")
		void shouldExtractIssuedAtClaimUsingGenericMethod() {
			// Given
			long beforeGeneration = System.currentTimeMillis();
			String token = jwtUtil.generateToken(TEST_EMAIL);
			long afterGeneration = System.currentTimeMillis();

			// When
			Date issuedAt = jwtUtil.extractClaim(token, Claims::getIssuedAt);

			// Then
			assertNotNull(issuedAt);
			// Add some tolerance for timing (1 second)
			assertTrue(issuedAt.getTime() >= beforeGeneration - 1000);
			assertTrue(issuedAt.getTime() <= afterGeneration + 1000);
		}

		@Test
		@DisplayName("Should extract ID claim using generic method")
		void shouldExtractIdClaimUsingGenericMethod() {
			// Given
			String token = jwtUtil.generateToken(TEST_EMAIL);

			// When
			String id = jwtUtil.extractClaim(token, Claims::getId);

			// Then
			assertNotNull(id);
			assertFalse(id.isEmpty());
			// Verify it's a valid UUID format
			assertDoesNotThrow(() -> java.util.UUID.fromString(id));
		}

		@Test
		@DisplayName("Should throw exception when extracting claim from invalid token")
		void shouldThrowExceptionWhenExtractingClaimFromInvalidToken() {
			// Given
			String invalidToken = "invalid.token.here";

			// When & Then
			Exception exception =
					assertThrows(
							MalformedJwtException.class,
							() -> jwtUtil.extractClaim(invalidToken, Claims::getSubject));
			assertNotNull(exception);
		}
	}

	@Nested
	@DisplayName("Integration Tests")
	class IntegrationTests {

		@Test
		@DisplayName("Should create, validate and extract data from token in complete flow")
		void shouldCreateValidateAndExtractDataFromTokenInCompleteFlow() {
			// Given
			String testEmail = "integration@test.com";

			// When - Generate token
			String token = jwtUtil.generateToken(testEmail);

			// Then - Validate token
			assertTrue(jwtUtil.validateToken(token));

			// And - Extract email
			assertEquals(testEmail, jwtUtil.extractEmail(token));

			// And - Extract expiration
			Date expiration = jwtUtil.extractExpiration(token);
			assertTrue(expiration.after(new Date()));

			// And - Extract custom claims
			String id = jwtUtil.extractClaim(token, Claims::getId);
			assertNotNull(id);
			assertDoesNotThrow(() -> java.util.UUID.fromString(id));
		}

		@Test
		@DisplayName("Should handle multiple tokens for different users")
		void shouldHandleMultipleTokensForDifferentUsers() {
			// Given
			String[] emails = {"user1@test.com", "user2@test.com", "user3@test.com"};

			// When
			String[] tokens = new String[emails.length];
			for (int i = 0; i < emails.length; i++) {
				tokens[i] = jwtUtil.generateToken(emails[i]);
			}

			// Then
			for (int i = 0; i < emails.length; i++) {
				assertTrue(jwtUtil.validateToken(tokens[i]));
				assertEquals(emails[i], jwtUtil.extractEmail(tokens[i]));
			}
		}
	}

	/**
	 * Helper method to set private fields using reflection
	 */
	private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}
}
