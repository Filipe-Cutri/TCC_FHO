package com.slotfy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service for handling file storage operations
 */
@Service
public class FileStorageService {
    
    private final Path fileStorageLocation;
    
    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Não foi possível criar o diretório de upload de arquivos.", ex);
        }
    }
    
    /**
     * Store a file and return its stored filename
     */
    public String storeFile(MultipartFile file, String folder) {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio não pode ser enviado");
        }
        
        // Get original filename and validate extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        
        if (!isValidImageExtension(fileExtension)) {
            throw new IllegalArgumentException("Apenas arquivos JPG e PNG são permitidos");
        }
        
        // Check file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Arquivo muito grande. Tamanho máximo: 5MB");
        }
        
        try {
            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "." + fileExtension;
            
            // Create folder if it doesn't exist
            Path folderPath = this.fileStorageLocation.resolve(folder);
            Files.createDirectories(folderPath);
            
            // Store file
            Path targetLocation = folderPath.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Return relative path
            return folder + "/" + filename;
        } catch (IOException ex) {
            throw new RuntimeException("Não foi possível armazenar o arquivo. Tente novamente!", ex);
        }
    }
    
    /**
     * Delete a file by its path
     */
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }
        
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            // Log error but don't throw exception for delete failures
            System.err.println("Erro ao deletar arquivo: " + filePath);
        }
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * Validate if file extension is allowed for images
     */
    private boolean isValidImageExtension(String extension) {
        return extension.equals("jpg") || 
               extension.equals("jpeg") || 
               extension.equals("png");
    }
    
    /**
     * Get the full file storage path
     */
    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }
}
