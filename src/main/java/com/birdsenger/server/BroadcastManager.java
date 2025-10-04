package com.birdsenger.server;

import com.birdsenger.network.Protocol;

import java.util.Map;

/**
 * Manages broadcasting messages to clients
 * Routes messages to specific users or all users
 */
public class BroadcastManager {
    private Map<Integer, ClientHandler> connectedClients;

    public BroadcastManager(Map<Integer, ClientHandler> connectedClients) {
        this.connectedClients = connectedClients;
    }

    /**
     * Send message to a specific user
     * @param userId Target user ID
     * @param message JSON message string
     * @return true if message was sent
     */
    public boolean sendMessageToUser(int userId, String message) {
        ClientHandler handler = connectedClients.get(userId);

        if (handler != null && handler.isRunning()) {
            handler.sendMessage(message);
            System.out.println("📤 Message sent to user " + userId);
            return true;
        } else {
            System.out.println("⚠️ User " + userId + " is not connected - message not delivered");
            return false;
        }
    }

    /**
     * Broadcast message to all connected clients
     * @param message JSON message string
     */
    public void broadcastToAll(String message) {
        int sentCount = 0;

        for (ClientHandler handler : connectedClients.values()) {
            if (handler.isRunning()) {
                handler.sendMessage(message);
                sentCount++;
            }
        }

        System.out.println("📢 Broadcast message sent to " + sentCount + " clients");
    }

    /**
     * Broadcast message to all clients except one
     * @param excludeUserId User ID to exclude
     * @param message JSON message string
     */
    public void broadcastToAllExcept(int excludeUserId, String message) {
        int sentCount = 0;

        for (Map.Entry<Integer, ClientHandler> entry : connectedClients.entrySet()) {
            if (entry.getKey() != excludeUserId && entry.getValue().isRunning()) {
                entry.getValue().sendMessage(message);
                sentCount++;
            }
        }

        System.out.println("📢 Broadcast message sent to " + sentCount + " clients (excluding " + excludeUserId + ")");
    }

    /**
     * Broadcast presence update to all clients
     * @param userId User whose status changed
     * @param status New status (ONLINE/OFFLINE)
     */
    public void broadcastPresenceUpdate(int userId, String status) {
        String presenceMessage = Protocol.createPresenceMessage(userId, status);

        // Send to all clients except the user whose status changed
        broadcastToAllExcept(userId, presenceMessage);

        System.out.println("💚 Presence update broadcasted: User " + userId + " is " + status);
    }

    /**
     * Send typing indicator to specific user
     * @param receiverId User who should see the typing indicator
     * @param senderId User who is typing
     * @param isTyping True if typing, false if stopped
     */
    public void sendTypingIndicator(int receiverId, int senderId, boolean isTyping) {
        String typingMessage = Protocol.createTypingMessage(senderId, receiverId, isTyping);
        sendMessageToUser(receiverId, typingMessage);

        System.out.println("⌨️ Typing indicator sent: " + senderId + " -> " + receiverId);
    }

    /**
     * Get count of connected clients
     * @return Number of connected clients
     */
    public int getConnectedClientCount() {
        return connectedClients.size();
    }

    /**
     * Check if a user is currently connected
     * @param userId User ID to check
     * @return true if user is connected
     */
    public boolean isUserOnline(int userId) {
        ClientHandler handler = connectedClients.get(userId);
        return handler != null && handler.isRunning();
    }

    /**
     * Get all connected user IDs
     * @return Array of user IDs
     */
    public Integer[] getConnectedUserIds() {
        return connectedClients.keySet().toArray(new Integer[0]);
    }
}