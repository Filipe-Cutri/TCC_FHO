package com.slotfy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {
    
    @TempDir
    Path tempDir;
    
    private FileStorageService fileStorageService;
    
    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }
    
    @Test
    void testStoreValidJpgFile() {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );
        
        // When
        String storedPath = fileStorageService.storeFile(file, "professionals");
        
        // Then
        assertNotNull(storedPath);
        assertTrue(storedPath.startsWith("professionals/"));
        assertTrue(storedPath.endsWith(".jpg"));
    }
    
    @Test
    void testStoreValidPngFile() {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            content
        );
        
        // When
        String storedPath = fileStorageService.storeFile(file, "services");
        
        // Then
        assertNotNull(storedPath);
        assertTrue(storedPath.startsWith("services/"));
        assertTrue(storedPath.endsWith(".png"));
    }
    
    @Test
    void testStoreValidJpegFile() {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.jpeg",
            "image/jpeg",
            content
        );
        
        // When
        String storedPath = fileStorageService.storeFile(file, "professionals");
        
        // Then
        assertNotNull(storedPath);
        assertTrue(storedPath.startsWith("professionals/"));
        assertTrue(storedPath.endsWith(".jpeg"));
    }
    
    @Test
    void testStoreEmptyFile() {
        // Given
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            new byte[0]
        );
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file, "professionals");
        });
    }
    
    @Test
    void testStoreInvalidFileExtension() {
        // Given
        byte[] content = "fake content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            content
        );
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file, "professionals");
        });
    }
    
    @Test
    void testStoreFileTooLarge() {
        // Given
        byte[] content = new byte[6 * 1024 * 1024]; // 6MB
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file, "professionals");
        });
    }
    
    @Test
    void testDeleteExistingFile() throws IOException {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );
        
        String storedPath = fileStorageService.storeFile(file, "professionals");
        Path fullPath = fileStorageService.getFileStorageLocation().resolve(storedPath);
        assertTrue(Files.exists(fullPath));
        
        // When
        fileStorageService.deleteFile(storedPath);
        
        // Then
        assertFalse(Files.exists(fullPath));
    }
    
    @Test
    void testDeleteNonExistentFile() {
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            fileStorageService.deleteFile("non-existent/file.jpg");
        });
    }
    
    @Test
    void testDeleteNullFilePath() {
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            fileStorageService.deleteFile(null);
        });
    }
    
    @Test
    void testDeleteEmptyFilePath() {
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            fileStorageService.deleteFile("");
        });
    }
    
    @Test
    void testDeleteWhitespaceFilePath() {
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            fileStorageService.deleteFile("   ");
        });
    }
    
    @Test
    void testStoreFileCreatesFolder() {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );
        
        // When
        String storedPath = fileStorageService.storeFile(file, "new-folder");
        
        // Then
        Path folderPath = fileStorageService.getFileStorageLocation().resolve("new-folder");
        assertTrue(Files.exists(folderPath));
        assertTrue(Files.isDirectory(folderPath));
    }
    
    @Test
    void testStoreFileGeneratesUniqueNames() {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file1 = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );
        MultipartFile file2 = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );
        
        // When
        String storedPath1 = fileStorageService.storeFile(file1, "professionals");
        String storedPath2 = fileStorageService.storeFile(file2, "professionals");
        
        // Then
        assertNotEquals(storedPath1, storedPath2);
    }
    
    @Test
    void testStoreFileRejectsPathTraversal() {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );
        
        // When & Then - path traversal attempts result in sanitized folder names
        // The sanitization removes path traversal characters and creates safe folder names
        String result1 = fileStorageService.storeFile(file, "../../../etc");
        assertNotNull(result1);
        assertTrue(result1.startsWith("etc/")); // "../../../" is removed, only "etc" remains
        
        String result2 = fileStorageService.storeFile(file, "../../uploads");
        assertNotNull(result2);
        assertTrue(result2.startsWith("uploads/")); // "../../" is removed, only "uploads" remains
    }
    
    @Test
    void testStoreFileRejectsInvalidFolderName() {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );
        
        // When & Then - completely invalid folder names are rejected
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file, "");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file, null);
        });
        
        // Folder name with only special characters becomes empty after sanitization
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file, "../../../");
        });
    }
    
    @Test
    void testStoreFileWithNullFilename() {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            null,
            "image/jpeg",
            content
        );
        
        // When & Then - null filename should be handled
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file, "professionals");
        });
    }
    
    @Test
    void testStoreFileWithNoExtension() {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "testfile",
            "image/jpeg",
            content
        );
        
        // When & Then - file without extension should be rejected
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file, "professionals");
        });
    }
    
    @Test
    void testGetFileStorageLocation() {
        // When
        Path location = fileStorageService.getFileStorageLocation();
        
        // Then
        assertNotNull(location);
        assertTrue(location.isAbsolute());
    }
    
    @Test
    void testStoreFileExactly5MB() {
        // Given - exactly 5MB should be allowed
        byte[] content = new byte[5 * 1024 * 1024]; // 5MB
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );
        
        // When - should not throw exception
        String storedPath = fileStorageService.storeFile(file, "professionals");
        
        // Then
        assertNotNull(storedPath);
    }
    
    @Test
    void testStoreFileWithSpecialCharactersInFolder() {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );
        
        // When - folder with special characters should be sanitized
        String storedPath = fileStorageService.storeFile(file, "test_folder-123");
        
        // Then
        assertNotNull(storedPath);
        assertTrue(storedPath.startsWith("test_folder-123/"));
    }
    
    @Test
    void testStoreFileGifExtension() {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.gif",
            "image/gif",
            content
        );
        
        // When & Then - GIF files should be rejected
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file, "professionals");
        });
    }
    
    @Test
    void testStoreFileBmpExtension() {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.bmp",
            "image/bmp",
            content
        );
        
        // When & Then - BMP files should be rejected
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file, "professionals");
        });
    }
    
    @Test
    void testStoreFileUppercaseExtension() {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.JPG",
            "image/jpeg",
            content
        );
        
        // When - uppercase extension should be converted to lowercase
        String storedPath = fileStorageService.storeFile(file, "professionals");
        
        // Then
        assertNotNull(storedPath);
        assertTrue(storedPath.endsWith(".jpg"));
    }
    
    @Test
    void testStoreFileMixedCaseExtension() {
        // Given
        byte[] content = "fake image content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.JpG",
            "image/jpeg",
            content
        );
        
        // When - mixed case extension should be converted to lowercase
        String storedPath = fileStorageService.storeFile(file, "professionals");
        
        // Then
        assertNotNull(storedPath);
        assertTrue(storedPath.endsWith(".jpg"));
    }
}
