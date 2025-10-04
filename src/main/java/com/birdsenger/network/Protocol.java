package com.birdsenger.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Protocol class defining the message format for client-server communication
 * Uses JSON format for all messages
 */
public class Protocol {
    private static final Gson gson = new Gson();

    // Message Types
    public static final String AUTH = "AUTH";
    public static final String MESSAGE = "MESSAGE";
    public static final String PRESENCE = "PRESENCE";
    public static final String USER_LIST = "USER_LIST";
    public static final String TYPING = "TYPING";
    public static final String ACK = "ACK";
    public static final String ERROR = "ERROR";

    // Auth Actions
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";

    // Presence Status
    public static final String ONLINE = "ONLINE";
    public static final String OFFLINE = "OFFLINE";

    /**
     * Create authentication message
     * @param userId User ID
     * @param username Username
     * @return JSON string
     */
    public static String createAuthMessage(int userId, String username) {
        JsonObject json = new JsonObject();
        json.addProperty("type", AUTH);
        json.addProperty("action", LOGIN);
        json.addProperty("userId", userId);
        json.addProperty("username", username);
        json.addProperty("timestamp", System.currentTimeMillis());
        return gson.toJson(json);
    }

    /**
     * Create logout message
     * @param userId User ID
     * @return JSON string
     */
    public static String createLogoutMessage(int userId) {
        JsonObject json = new JsonObject();
        json.addProperty("type", AUTH);
        json.addProperty("action", LOGOUT);
        json.addProperty("userId", userId);
        json.addProperty("timestamp", System.currentTimeMillis());
        return gson.toJson(json);
    }

    /**
     * Create chat message
     * @param senderId Sender user ID
     * @param receiverId Receiver user ID
     * @param content Message content
     * @return JSON string
     */
    public static String createChatMessage(int senderId, int receiverId, String content) {
        JsonObject json = new JsonObject();
        json.addProperty("type", MESSAGE);
        json.addProperty("senderId", senderId);
        json.addProperty("receiverId", receiverId);
        json.addProperty("content", content);
        json.addProperty("timestamp", System.currentTimeMillis());
        return gson.toJson(json);
    }

    /**
     * Create presence update message
     * @param userId User ID
     * @param status Online status (ONLINE/OFFLINE)
     * @return JSON string
     */
    public static String createPresenceMessage(int userId, String status) {
        JsonObject json = new JsonObject();
        json.addProperty("type", PRESENCE);
        json.addProperty("userId", userId);
        json.addProperty("status", status);
        json.addProperty("timestamp", System.currentTimeMillis());
        return gson.toJson(json);
    }

    /**
     * Create typing indicator message
     * @param senderId Sender user ID
     * @param receiverId Receiver user ID
     * @param isTyping True if typing, false if stopped
     * @return JSON string
     */
    public static String createTypingMessage(int senderId, int receiverId, boolean isTyping) {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPING);
        json.addProperty("senderId", senderId);
        json.addProperty("receiverId", receiverId);
        json.addProperty("isTyping", isTyping);
        json.addProperty("timestamp", System.currentTimeMillis());
        return gson.toJson(json);
    }

    /**
     * Create acknowledgment message
     * @param messageId Original message ID/type
     * @param success True if successful
     * @return JSON string
     */
    public static String createAckMessage(String messageId, boolean success) {
        JsonObject json = new JsonObject();
        json.addProperty("type", ACK);
        json.addProperty("messageId", messageId);
        json.addProperty("success", success);
        json.addProperty("timestamp", System.currentTimeMillis());
        return gson.toJson(json);
    }

    /**
     * Create error message
     * @param errorMessage Error description
     * @return JSON string
     */
    public static String createErrorMessage(String errorMessage) {
        JsonObject json = new JsonObject();
        json.addProperty("type", ERROR);
        json.addProperty("message", errorMessage);
        json.addProperty("timestamp", System.currentTimeMillis());
        return gson.toJson(json);
    }

    /**
     * Parse JSON message to JsonObject
     * @param jsonString JSON string
     * @return JsonObject
     */
    public static JsonObject parseMessage(String jsonString) {
        try {
            return gson.fromJson(jsonString, JsonObject.class);
        } catch (Exception e) {
            System.err.println("❌ Failed to parse message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get message type from JSON
     * @param json JsonObject
     * @return Message type string
     */
    public static String getMessageType(JsonObject json) {
        if (json != null && json.has("type")) {
            return json.get("type").getAsString();
        }
        return null;
    }
}