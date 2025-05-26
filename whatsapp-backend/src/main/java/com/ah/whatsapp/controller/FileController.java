/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ah.whatsapp.enums.FolderName;
import com.ah.whatsapp.service.FileStorage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorage fileStorage;

    @GetMapping("/profile-pictures/{filename:.+}")
    public ResponseEntity<Resource> getProfilePicture(
            @PathVariable(name = "filename") String filename, HttpServletRequest request) {
        log.debug("Request to get profile picture: {}", filename);
        try {
            Resource resource =
                    fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, filename);

            String contentType = null;
            try {
                contentType =
                        request.getServletContext()
                                .getMimeType(resource.getFile().getAbsolutePath());
                log.debug("Determined content type: {} for file: {}", contentType, filename);
            } catch (IOException ex) {
                log.warn("Could not determine content type for file: {}", filename, ex);
            }
            // Fallback to a default content type if unable to determine
            if (contentType == null) {
                contentType = "application/octet-stream";
                log.debug("Using default content type for file: {}", filename);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                    .body(resource);

        } catch (MalformedURLException ex) {
            log.error("Malformed URL for file: {} or base storage path.", filename, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException ex) {
            log.warn("Profile picture not found: {}", filename, ex);
            return ResponseEntity.notFound().build();
        }
    }
}
