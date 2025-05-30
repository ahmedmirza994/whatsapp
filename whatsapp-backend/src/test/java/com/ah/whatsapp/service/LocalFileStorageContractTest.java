/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.service;

import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;

import com.ah.whatsapp.service.impl.LocalFileStorage;

/**
 * Contract test implementation for LocalFileStorage.
 * This ensures LocalFileStorage follows the FileStorage contract.
 *
 * Inherits all contract tests from FileStorageContractTest and runs them
 * against the LocalFileStorage implementation.
 */
class LocalFileStorageContractTest extends FileStorageContractTest {

    @TempDir Path tempDir;

    @Override
    protected FileStorage createFileStorage() {
        return new LocalFileStorage(tempDir.toString());
    }
}
