package com.example.legacyapp.util;

import com.example.legacyapp.model.User;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class FileStorageService {

    private static final String DATA_DIR = "data";
    private static final String USER_FILE = "data/users.txt";
    private static final String BACKUP_DIR = "data/backups";
    private static final String UPLOAD_DIR = "data/uploads";

    @PostConstruct
    public void init() {
        createDirectories();
    }

    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(BACKUP_DIR));
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            System.err.println("Error creating directories: " + e.getMessage());
        }
    }

    public void saveUserToFile(User user) {
        try {
            String userData = String.format("%d,%s,%s,%s%n",
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            Files.write(Paths.get(USER_FILE), 
                       userData.getBytes(StandardCharsets.UTF_8),
                       StandardOpenOption.CREATE,
                       StandardOpenOption.APPEND);
            
            createBackup();
        } catch (IOException e) {
            System.err.println("Error saving user to file: " + e.getMessage());
        }
    }

    public void updateUserFile(User user) {
        try {
            File file = new File(USER_FILE);
            if (file.exists()) {
                String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                String[] lines = content.split("\n");
                StringBuilder updatedContent = new StringBuilder();
                
                for (String line : lines) {
                    if (line.startsWith(user.getId() + ",")) {
                        updatedContent.append(String.format("%d,%s,%s,%s%n",
                                user.getId(),
                                user.getUsername(),
                                user.getEmail(),
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                    } else if (!line.isEmpty()) {
                        updatedContent.append(line).append("\n");
                    }
                }
                
                FileUtils.writeStringToFile(file, updatedContent.toString(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.err.println("Error updating user file: " + e.getMessage());
        }
    }

    public void deleteUserFile(User user) {
        try {
            File file = new File(USER_FILE);
            if (file.exists()) {
                String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                String[] lines = content.split("\n");
                StringBuilder updatedContent = new StringBuilder();
                
                for (String line : lines) {
                    if (!line.startsWith(user.getId() + ",") && !line.isEmpty()) {
                        updatedContent.append(line).append("\n");
                    }
                }
                
                FileUtils.writeStringToFile(file, updatedContent.toString(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.err.println("Error deleting user from file: " + e.getMessage());
        }
    }

    private void createBackup() {
        try {
            File sourceFile = new File(USER_FILE);
            if (sourceFile.exists()) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File backupFile = new File(BACKUP_DIR + "/users_" + timestamp + ".txt");
                FileUtils.copyFile(sourceFile, backupFile);
            }
        } catch (IOException e) {
            System.err.println("Error creating backup: " + e.getMessage());
        }
    }

    public void saveUploadedFile(String filename, byte[] content) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR, filename);
        Files.write(uploadPath, content);
    }

    public byte[] readUploadedFile(String filename) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR, filename);
        return Files.readAllBytes(uploadPath);
    }
}