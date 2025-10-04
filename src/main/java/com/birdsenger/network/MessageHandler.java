package com.birdsenger.network;

import com.birdsenger.dao.MessageDAO;
import com.birdsenger.model.Message;
import com.birdsenger.model.MessageType;
import com.google.gson.JsonObject;
import javafx.application.Platform;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Consumer;

/**
 * Handles parsing and routing of incoming messages from server
 * Routes messages to appropriate handlers based on type
 */
public class MessageHandler {
    private MessageDAO messageDAO;
    private Consumer<Message> onNewMessageReceived;
    private Consumer<String> onUserStatusChanged;
    private Consumer<String> onTypingIndicator;
    private Consumer<String> onError;

    public MessageHandler() {
        this.messageDAO = new MessageDAO();
    }

    /**
     * Process incoming message from server
     * @param jsonString JSON message string
     */
    public void handleIncomingMessage(String jsonString) {
        try {
            JsonObject json = Protocol.parseMessage(jsonString);

            if (json == null) {
                System.err.println("❌ Received invalid JSON message");
                return;
            }

            String type = Protocol.getMessageType(json);

            if (type == null) {
                System.err.println("❌ Message has no type field");
                return;
            }

            // Route to appropriate handler based on type
            switch (type) {
                case Protocol.MESSAGE -> handleChatMessage(json);
                case Protocol.PRESENCE -> handlePresenceUpdate(json);
                case Protocol.TYPING -> handleTypingIndicator(json);
                case Protocol.USER_LIST -> handleUserList(json);
                case Protocol.ACK -> handleAcknowledgment(json);
                case Protocol.ERROR -> handleError(json);
                default -> System.err.println("⚠️ Unknown message type: " + type);
            }

        } catch (Exception e) {
            System.err.println("❌ Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle incoming chat message
     */
    private void handleChatMessage(JsonObject json) {
        try {
            int senderId = json.get("senderId").getAsInt();
            int receiverId = json.get("receiverId").getAsInt();
            String content = json.get("content").getAsString();
            long timestamp = json.get("timestamp").getAsLong();

            // Convert timestamp to LocalDateTime
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp),
                    ZoneId.systemDefault()
            );

            // Create Message object
            Message message = new Message(
                    senderId,
                    receiverId,
                    content,
                    MessageType.TEXT
            );
            message.setTimestamp(dateTime);

            // Save to database
            messageDAO.saveMessage(message);

            System.out.println("✅ Received message from user " + senderId + ": " + content);

            // Notify UI to update (must run on JavaFX thread)
            if (onNewMessageReceived != null) {
                Platform.runLater(() -> onNewMessageReceived.accept(message));
            }

        } catch (Exception e) {
            System.err.println("❌ Error processing chat message: " + e.getMessage());
        }
    }

    /**
     * Handle presence update (user online/offline)
     */
    private void handlePresenceUpdate(JsonObject json) {
        try {
            int userId = json.get("userId").getAsInt();
            String status = json.get("status").getAsString();

            System.out.println("👤 User " + userId + " is now " + status);

            // Notify UI
            if (onUserStatusChanged != null) {
                String statusUpdate = userId + ":" + status;
                Platform.runLater(() -> onUserStatusChanged.accept(statusUpdate));
            }

        } catch (Exception e) {
            System.err.println("❌ Error processing presence update: " + e.getMessage());
        }
    }

    /**
     * Handle typing indicator
     */
    private void handleTypingIndicator(JsonObject json) {
        try {
            int senderId = json.get("senderId").getAsInt();
            boolean isTyping = json.get("isTyping").getAsBoolean();

            String indicator = senderId + ":" + (isTyping ? "typing" : "stopped");

            System.out.println("⌨️ User " + senderId + " is " + (isTyping ? "typing..." : "not typing"));

            // Notify UI
            if (onTypingIndicator != null) {
                Platform.runLater(() -> onTypingIndicator.accept(indicator));
            }

        } catch (Exception e) {
            System.err.println("❌ Error processing typing indicator: " + e.getMessage());
        }
    }

    /**
     * Handle user list update from server
     */
    private void handleUserList(JsonObject json) {
        try {
            // For Phase 2, we'll implement this when needed
            System.out.println("📋 Received user list update");
        } catch (Exception e) {
            System.err.println("❌ Error processing user list: " + e.getMessage());
        }
    }

    /**
     * Handle acknowledgment from server
     */
    private void handleAcknowledgment(JsonObject json) {
        try {
            String messageId = json.get("messageId").getAsString();
            boolean success = json.get("success").getAsBoolean();

            if (success) {
                System.out.println("✅ Server acknowledged: " + messageId);
            } else {
                System.err.println("❌ Server rejected: " + messageId);
            }

        } catch (Exception e) {
            System.err.println("❌ Error processing acknowledgment: " + e.getMessage());
        }
    }

    /**
     * Handle error message from server
     */
    private void handleError(JsonObject json) {
        try {
            String errorMessage = json.get("message").getAsString();
            System.err.println("❌ Server error: " + errorMessage);

            // Notify UI
            if (onError != null) {
                Platform.runLater(() -> onError.accept(errorMessage));
            }

        } catch (Exception e) {
            System.err.println("❌ Error processing error message: " + e.getMessage());
        }
    }

    // Setters for callbacks

    /**
     * Set callback for when a new message is received
     */
    public void setOnNewMessageReceived(Consumer<Message> callback) {
        this.onNewMessageReceived = callback;
    }

    /**
     * Set callback for user status changes
     */
    public void setOnUserStatusChanged(Consumer<String> callback) {
        this.onUserStatusChanged = callback;
    }

    /**
     * Set callback for typing indicators
     */
    public void setOnTypingIndicator(Consumer<String> callback) {
        this.onTypingIndicator = callback;
    }

    /**
     * Set callback for errors
     */
    public void setOnError(Consumer<String> callback) {
        this.onError = callback;
    }
}
