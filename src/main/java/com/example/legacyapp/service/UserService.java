package com.example.legacyapp.service;

import com.example.legacyapp.model.User;
import com.example.legacyapp.util.FileStorageService;
import com.example.legacyapp.util.Java8Features;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final Map<Long, User> userDatabase = new ConcurrentHashMap<>();
    private Long currentId = 1L;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private Java8Features java8Features;

    @PostConstruct
    public void init() {
        loadUsersFromFile();
    }

    private void loadUsersFromFile() {
        try {
            File dataFile = new File("data/users.txt");
            if (dataFile.exists()) {
                List<String> lines = FileUtils.readLines(dataFile, StandardCharsets.UTF_8);
                lines.forEach(line -> {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        User user = new User();
                        user.setId(Long.parseLong(parts[0]));
                        user.setUsername(parts[1]);
                        user.setEmail(parts[2]);
                        userDatabase.put(user.getId(), user);
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    public List<User> getAllUsers() {
        return ImmutableList.copyOf(userDatabase.values());
    }

    public java.util.Optional<User> getUserById(Long id) {
        Optional<User> guavaOptional = Optional.fromNullable(userDatabase.get(id));
        return guavaOptional.isPresent() 
            ? java.util.Optional.of(guavaOptional.get())
            : java.util.Optional.empty();
    }

    public User createUser(User user) {
        user.setId(currentId++);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        userDatabase.put(user.getId(), user);
        fileStorageService.saveUserToFile(user);
        
        java8Features.processUserWithLambda(user);
        
        return user;
    }

    public java.util.Optional<User> updateUser(Long id, User updatedUser) {
        if (userDatabase.containsKey(id)) {
            User existingUser = userDatabase.get(id);
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setUpdatedAt(new Date());
            fileStorageService.updateUserFile(existingUser);
            return java.util.Optional.of(existingUser);
        }
        return java.util.Optional.empty();
    }

    public boolean deleteUser(Long id) {
        if (userDatabase.containsKey(id)) {
            User user = userDatabase.remove(id);
            fileStorageService.deleteUserFile(user);
            return true;
        }
        return false;
    }

    public List<User> searchByUsername(String username) {
        return userDatabase.values().stream()
                .filter(user -> user.getUsername().toLowerCase()
                        .contains(username.toLowerCase()))
                .collect(Collectors.toList());
    }
}