package com.birdsenger.model;

/**
 * Represents user online status
 * Used for presence indicators in the UI
 */
public enum PresenceStatus {
    ONLINE("Online"),
    OFFLINE("Offline"),
    AWAY("Away"),
    DO_NOT_DISTURB("Do Not Disturb");

    private final String displayName;

    PresenceStatus(String displayName) {
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