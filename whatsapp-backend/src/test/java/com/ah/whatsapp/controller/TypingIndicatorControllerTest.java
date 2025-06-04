/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.ah.whatsapp.dto.TypingIndicatorDto;
import com.ah.whatsapp.dto.TypingIndicatorDtoTestDataBuilder;
import com.ah.whatsapp.enums.EventType;

@ExtendWith(MockitoExtension.class)
@DisplayName("TypingIndicatorController Unit Tests")
class TypingIndicatorControllerTest {

	@Mock private ApplicationEventPublisher applicationEventPublisher;

	@InjectMocks private TypingIndicatorController typingIndicatorController;

	// Test data constants
	private static final UUID TEST_CONVERSATION_ID = UUID.randomUUID();
	private static final UUID TEST_USER_ID = UUID.randomUUID();

	@Nested
	@DisplayName("Constructor Tests")
	class ConstructorTests {

		@Test
		@DisplayName("Should initialize controller with ApplicationEventPublisher dependency")
		void shouldInitializeWithDependencies() {
			// Then
			assertNotNull(typingIndicatorController);
		}
	}

	@Nested
	@DisplayName("handleTyping - Success Cases")
	class HandleTypingSuccessCases {

		@Test
		@DisplayName("Should publish typing start event successfully")
		void shouldPublishTypingStartEvent() {
			// Given
			TypingIndicatorDto typingDto =
					TypingIndicatorDtoTestDataBuilder.aTypingIndicatorDto()
							.withConversationId(TEST_CONVERSATION_ID)
							.withUserId(TEST_USER_ID)
							.withEventType(EventType.TYPING_START)
							.build();

			// When
			typingIndicatorController.handleTyping(typingDto);

			// Then
			verify(applicationEventPublisher).publishEvent(typingDto);
			verifyNoMoreInteractions(applicationEventPublisher);
		}

		@Test
		@DisplayName("Should publish typing stop event successfully")
		void shouldPublishTypingStopEvent() {
			// Given
			TypingIndicatorDto typingDto =
					TypingIndicatorDtoTestDataBuilder.aTypingIndicatorDto()
							.withConversationId(TEST_CONVERSATION_ID)
							.withUserId(TEST_USER_ID)
							.withEventType(EventType.TYPING_STOP)
							.build();

			// When
			typingIndicatorController.handleTyping(typingDto);

			// Then
			verify(applicationEventPublisher).publishEvent(typingDto);
			verifyNoMoreInteractions(applicationEventPublisher);
		}

		@Test
		@DisplayName("Should publish event with exact same DTO object that was provided")
		void shouldPublishEventWithExactSameDto() {
			// Given
			TypingIndicatorDto typingDto =
					TypingIndicatorDtoTestDataBuilder.aTypingIndicatorDto()
							.withConversationId(TEST_CONVERSATION_ID)
							.withUserId(TEST_USER_ID)
							.withTypingStart()
							.build();

			// When
			typingIndicatorController.handleTyping(typingDto);

			// Then - Verify the exact same object is passed to the event publisher
			verify(applicationEventPublisher).publishEvent(typingDto);
		}

		@Test
		@DisplayName("Should handle multiple different conversation IDs correctly")
		void shouldHandleMultipleConversations() {
			// Given
			UUID firstConversationId = UUID.randomUUID();
			UUID secondConversationId = UUID.randomUUID();

			TypingIndicatorDto firstTypingDto =
					TypingIndicatorDtoTestDataBuilder.aTypingIndicatorDto()
							.withConversationId(firstConversationId)
							.withUserId(TEST_USER_ID)
							.withTypingStart()
							.build();

			TypingIndicatorDto secondTypingDto =
					TypingIndicatorDtoTestDataBuilder.aTypingIndicatorDto()
							.withConversationId(secondConversationId)
							.withUserId(TEST_USER_ID)
							.withTypingStop()
							.build();

			// When
			typingIndicatorController.handleTyping(firstTypingDto);
			typingIndicatorController.handleTyping(secondTypingDto);

			// Then
			verify(applicationEventPublisher).publishEvent(firstTypingDto);
			verify(applicationEventPublisher).publishEvent(secondTypingDto);
		}

		@Test
		@DisplayName("Should handle multiple different user IDs correctly")
		void shouldHandleMultipleUsers() {
			// Given
			UUID firstUserId = UUID.randomUUID();
			UUID secondUserId = UUID.randomUUID();

			TypingIndicatorDto firstTypingDto =
					TypingIndicatorDtoTestDataBuilder.aTypingIndicatorDto()
							.withConversationId(TEST_CONVERSATION_ID)
							.withUserId(firstUserId)
							.withTypingStart()
							.build();

			TypingIndicatorDto secondTypingDto =
					TypingIndicatorDtoTestDataBuilder.aTypingIndicatorDto()
							.withConversationId(TEST_CONVERSATION_ID)
							.withUserId(secondUserId)
							.withTypingStop()
							.build();

			// When
			typingIndicatorController.handleTyping(firstTypingDto);
			typingIndicatorController.handleTyping(secondTypingDto);

			// Then
			verify(applicationEventPublisher).publishEvent(firstTypingDto);
			verify(applicationEventPublisher).publishEvent(secondTypingDto);
		}
	}

	@Nested
	@DisplayName("handleTyping - Edge Cases")
	class HandleTypingEdgeCases {

		@Test
		@DisplayName("Should publish event even when DTO has minimum valid data")
		void shouldPublishEventWithMinimalValidData() {
			// Given - Using builder with default values (all required fields populated)
			TypingIndicatorDto typingDto =
					TypingIndicatorDtoTestDataBuilder.aTypingIndicatorDto().build();

			// When
			typingIndicatorController.handleTyping(typingDto);

			// Then
			verify(applicationEventPublisher).publishEvent(typingDto);
		}

		@Test
		@DisplayName("Should handle rapid successive typing events correctly")
		void shouldHandleRapidSuccessiveEvents() {
			// Given
			TypingIndicatorDto startTypingDto =
					TypingIndicatorDtoTestDataBuilder.aTypingIndicatorDto()
							.withConversationId(TEST_CONVERSATION_ID)
							.withUserId(TEST_USER_ID)
							.withTypingStart()
							.build();

			TypingIndicatorDto stopTypingDto =
					TypingIndicatorDtoTestDataBuilder.aTypingIndicatorDto()
							.withConversationId(TEST_CONVERSATION_ID)
							.withUserId(TEST_USER_ID)
							.withTypingStop()
							.build();

			// When - Simulate rapid start/stop/start typing sequence
			typingIndicatorController.handleTyping(startTypingDto);
			typingIndicatorController.handleTyping(stopTypingDto);
			typingIndicatorController.handleTyping(
					startTypingDto); // Same object reused (realistic scenario)

			// Then - Verify all events were published
			verify(applicationEventPublisher, times(2))
					.publishEvent(startTypingDto); // Called twice with start
			verify(applicationEventPublisher, times(1))
					.publishEvent(stopTypingDto); // Called once with stop
		}
	}

	@Nested
	@DisplayName("handleTyping - Integration with Event System")
	class HandleTypingEventSystemIntegration {

		@Test
		@DisplayName("Should integrate properly with Spring's ApplicationEventPublisher")
		void shouldIntegrateWithApplicationEventPublisher() {
			// Given
			TypingIndicatorDto typingDto =
					TypingIndicatorDtoTestDataBuilder.aTypingIndicatorDto()
							.withConversationId(TEST_CONVERSATION_ID)
							.withUserId(TEST_USER_ID)
							.withEventType(EventType.TYPING_START)
							.build();

			// When
			typingIndicatorController.handleTyping(typingDto);

			// Then - Verify that the event publisher is called exactly once with the correct object
			verify(applicationEventPublisher).publishEvent(typingDto);
			verifyNoMoreInteractions(applicationEventPublisher);
		}
	}
}
