package com.birdsenger.server;

import com.birdsenger.network.Protocol;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles communication with a single client
 * Runs in its own thread
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BirdSengerServer server;
    private BroadcastManager broadcastManager;

    private PrintWriter out;
    private BufferedReader in;

    private Integer userId;
    private String username;
    private boolean running;

    public ClientHandler(Socket socket, BirdSengerServer server, BroadcastManager broadcastManager) {
        this.clientSocket = socket;
        this.server = server;
        this.broadcastManager = broadcastManager;
        this.running = true;
    }

    @Override
    public void run() {
        try {
            // Setup I/O streams
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("👤 ClientHandler started for: " + clientSocket.getInetAddress());

            // Read messages from client
            String message;
            while (running && (message = in.readLine()) != null) {
                handleMessage(message);
            }

        } catch (IOException e) {
            System.err.println("❌ ClientHandler error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * Handle incoming message from client
     */
    private void handleMessage(String jsonString) {
        try {
            JsonObject json = Protocol.parseMessage(jsonString);

            if (json == null) {
                sendError("Invalid message format");
                return;
            }

            String type = Protocol.getMessageType(json);

            if (type == null) {
                sendError("Message type missing");
                return;
            }

            System.out.println("📨 Received " + type + " from client");

            // Route based on message type
            switch (type) {
                case Protocol.AUTH -> handleAuth(json);
                case Protocol.MESSAGE -> handleChatMessage(json);
                case Protocol.PRESENCE -> handlePresence(json);
                case Protocol.TYPING -> handleTyping(json);
                default -> System.err.println("⚠️ Unknown message type: " + type);
            }

        } catch (Exception e) {
            System.err.println("❌ Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle authentication message
     */
    private void handleAuth(JsonObject json) {
        try {
            String action = json.get("action").getAsString();

            if (Protocol.LOGIN.equals(action)) {
                userId = json.get("userId").getAsInt();
                username = json.get("username").getAsString();

                // Register this client with server
                server.registerClient(userId, this);

                System.out.println("✅ User authenticated: " + username + " (ID: " + userId + ")");

                // Send acknowledgment
                String ack = Protocol.createAckMessage("AUTH", true);
                sendMessage(ack);

            } else if (Protocol.LOGOUT.equals(action)) {
                System.out.println("👋 User logging out: " + username);
                disconnect();
            }

        } catch (Exception e) {
            System.err.println("❌ Error handling auth: " + e.getMessage());
            sendError("Authentication failed");
        }
    }

    /**
     * Handle chat message - route to recipient
     */
    private void handleChatMessage(JsonObject json) {
        try {
            int senderId = json.get("senderId").getAsInt();
            int receiverId = json.get("receiverId").getAsInt();
            String content = json.get("content").getAsString();

            System.out.println("💬 Message from " + senderId + " to " + receiverId + ": " + content);

            // Route message to recipient
            broadcastManager.sendMessageToUser(receiverId, json.toString());

            // Send acknowledgment to sender
            String ack = Protocol.createAckMessage("MESSAGE", true);
            sendMessage(ack);

        } catch (Exception e) {
            System.err.println("❌ Error handling chat message: " + e.getMessage());
            sendError("Failed to send message");
        }
    }

    /**
     * Handle presence update
     */
    private void handlePresence(JsonObject json) {
        try {
            int userId = json.get("userId").getAsInt();
            String status = json.get("status").getAsString();

            System.out.println("💚 Presence update: User " + userId + " is " + status);

            // Broadcast to all clients
            broadcastManager.broadcastPresenceUpdate(userId, status);

        } catch (Exception e) {
            System.err.println("❌ Error handling presence: " + e.getMessage());
        }
    }

    /**
     * Handle typing indicator
     */
    private void handleTyping(JsonObject json) {
        try {
            int senderId = json.get("senderId").getAsInt();
            int receiverId = json.get("receiverId").getAsInt();
            boolean isTyping = json.get("isTyping").getAsBoolean();

            System.out.println("⌨️ Typing indicator: " + senderId + " -> " + receiverId);

            // Forward to recipient only
            broadcastManager.sendMessageToUser(receiverId, json.toString());

        } catch (Exception e) {
            System.err.println("❌ Error handling typing: " + e.getMessage());
        }
    }

    /**
     * Send message to this client
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    /**
     * Send error message to client
     */
    private void sendError(String errorMessage) {
        String error = Protocol.createErrorMessage(errorMessage);
        sendMessage(error);
    }

    /**
     * Disconnect this client
     */
    public void disconnect() {
        running = false;

        try {
            // Unregister from server
            if (userId != null) {
                server.unregisterClient(userId);
            }

            // Close streams
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }

            System.out.println("🔌 Client disconnected: " + (username != null ? username : "Unknown"));

        } catch (IOException e) {
            System.err.println("❌ Error disconnecting client: " + e.getMessage());
        }
    }

    /**
     * Get user ID
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Get username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Check if this handler is running
     */
    public boolean isRunning() {
        return running;
    }
}