/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.integration;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.ah.whatsapp.config.TestContainerConfig;

/**
 * Base class for integration tests using TestContainers.
 * This class sets up a PostgreSQL container for testing JPA repositories.
 *
 * <p>Key features:
 * - Uses real PostgreSQL database via TestContainers
 * - Automatic database cleanup between tests
 * - Shared container instance for performance
 * - Proper transaction management
 *
 * <p>Usage:
 * Extend this class in your repository integration tests to get
 * a fully configured test environment with PostgreSQL.
 */
@SpringBootTest
@Import(TestContainerConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public abstract class BaseIntegrationTest {}
