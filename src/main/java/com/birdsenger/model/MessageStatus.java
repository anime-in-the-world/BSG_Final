package com.birdsenger.model;

/**
 * Represents the delivery status of a message
 * SENT - Message sent from client
 * DELIVERED - Message reached recipient's device
 * READ - Message was read by recipient
 */
public enum MessageStatus {
    SENT("Sent"),
    DELIVERED("Delivered"),
    READ("Read");

    private final String displayName;

    MessageStatus(String displayName) {
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