package com.ah.whatsapp.configuration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.ah.whatsapp.model.Conversation;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.model.Message;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.ConversationParticipantRepository;
import com.ah.whatsapp.repository.ConversationRepository;
import com.ah.whatsapp.repository.MessageRepository;
import com.ah.whatsapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@Slf4j
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;
    private final Faker faker = new Faker(); // In

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		log.info("Starting data seeding for 'dev' profile...");

        if (userRepository.count() > 0) {
            log.info("Database already contains data. Skipping seeding.");
            return;
        }

		// === Create Users ===
        log.info("Creating mock users...");
        List<User> users = new ArrayList<>();
        // Create a known user for easy login
        User knownUser = new User();
        knownUser.setName("Test User");
        knownUser.setEmail("test@example.com");
        knownUser.setPassword(passwordEncoder.encode("password")); // Simple password for dev
        knownUser.setCreatedAt(LocalDateTime.now());
        knownUser.setUpdatedAt(LocalDateTime.now());
		knownUser.setPhone(faker.phoneNumber().phoneNumberInternational());
		knownUser = userRepository.save(knownUser);
        users.add(knownUser);

		// Create some fake users
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setName(faker.name().fullName());
            user.setEmail(faker.internet().emailAddress());
            user.setPassword(passwordEncoder.encode("password")); // Simple password for dev
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
	        knownUser.setPhone(faker.phoneNumber().phoneNumberInternational());
            users.add(userRepository.save(user));
        }
        log.info("{} users created.", users.size());

		log.info("Creating conversations and participants...");
        // Create conversations between the known user and some fake users
        for (int i = 1; i < 5; i++) { // Create 4 conversations for the known user
            User participant1 = knownUser;
            User participant2 = users.get(i);

            Conversation conversation = new Conversation();
            conversation.setCreatedAt(LocalDateTime.now());
            conversation.setUpdatedAt(LocalDateTime.now());
            Conversation savedConversation = conversationRepository.save(conversation);

            // Add participant 1
            ConversationParticipant cp1 = new ConversationParticipant();
            cp1.setConversationId(savedConversation.getId());
            cp1.setParticipantId(participant1.getId()); // Use User ID
            cp1.setJoinedAt(LocalDateTime.now());
            participantRepository.save(cp1);

            // Add participant 2
            ConversationParticipant cp2 = new ConversationParticipant();
            cp2.setConversationId(savedConversation.getId());
            cp2.setParticipantId(participant2.getId()); // Use User ID
            cp2.setJoinedAt(LocalDateTime.now());
            participantRepository.save(cp2);

            // === Create Messages for this conversation ===
            log.info("Creating messages for conversation {}...", savedConversation.getId());
            LocalDateTime lastMessageTime = null;
            for (int j = 0; j < faker.number().numberBetween(5, 15); j++) {
                Message message = new Message();
                message.setConversationId(savedConversation.getId());
                // Alternate sender
                message.setSender(j % 2 == 0 ? participant1 : participant2);
                message.setContent(faker.lorem().sentence(faker.number().numberBetween(3, 12)));
                // Ensure messages are sequential
                LocalDateTime sentAt = faker.date().past(30, TimeUnit.DAYS).toInstant()
                                            .atZone(ZoneId.systemDefault()).toLocalDateTime();
                message.setSentAt(sentAt);
                messageRepository.save(message);
                if (lastMessageTime == null || sentAt.isAfter(lastMessageTime)) {
                    lastMessageTime = sentAt;
                }
            }
            // Update conversation's updatedAt with the last message time
            if (lastMessageTime != null) {
                savedConversation.setUpdatedAt(lastMessageTime);
                conversationRepository.save(savedConversation);
            }
        }

        log.info("Data seeding finished.");
	}

}
