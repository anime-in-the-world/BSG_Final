package com.birdsenger.model;

import java.time.LocalDateTime;

/**
 * User entity representing a BirdSenger user
 * Maps to the 'users' table in SQLite database
 */
public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private String avatarPath;
    private String bio;
    private PresenceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeen;

    // Constructor for creating new user (before DB insert)
    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = PresenceStatus.OFFLINE;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for loading from database (with ID)
    public User(int id, String username, String email, String passwordHash,
                String avatarPath, String bio, PresenceStatus status,
                LocalDateTime createdAt, LocalDateTime lastSeen) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.avatarPath = avatarPath;
        this.bio = bio;
        this.status = status;
        this.createdAt = createdAt;
        this.lastSeen = lastSeen;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public PresenceStatus getStatus() {
        return status;
    }

    public void setStatus(PresenceStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                '}';
    }
}