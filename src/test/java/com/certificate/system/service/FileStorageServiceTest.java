package com.certificate.system.service;

import com.certificate.system.exception.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() throws IOException {
        fileStorageService = new FileStorageService();
        Path tempDir = Files.createTempDirectory("uploads");
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
        fileStorageService.init();
    }

    @Test
    void storeFile_validPdf_storesSuccessfully() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy content".getBytes());
        String storedName = fileStorageService.storeFile(file);
        assertTrue(storedName.endsWith(".pdf"));
    }

    @Test
    void storeFile_invalidExtension_throwsException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.exe", "application/octet-stream", "dummy content".getBytes());
        assertThrows(FileStorageException.class, () -> fileStorageService.storeFile(file));
    }

    @Test
    void storeFile_emptyFile_throwsException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[0]);
        assertThrows(FileStorageException.class, () -> fileStorageService.storeFile(file));
    }
}

