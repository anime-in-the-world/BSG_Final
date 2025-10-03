package com.birdsenger.model;

/**
 * Represents the type of message content
 * TEXT - Regular text message
 * IMAGE - Image file
 * FILE - Any file attachment
 * SYSTEM - System notification (user joined, etc.)
 */
public enum MessageType {
    TEXT("Text"),
    IMAGE("Image"),
    FILE("File"),
    SYSTEM("System");

    private final String displayName;

    MessageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}