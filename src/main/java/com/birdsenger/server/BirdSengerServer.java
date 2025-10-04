package com.birdsenger.server;

import com.birdsenger.network.Protocol;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main BirdSenger server
 * Accepts client connections and manages message routing
 */
public class BirdSengerServer {
    private static final int PORT = 8080;

    private ServerSocket serverSocket;
    private boolean running;

    // Map of userId -> ClientHandler
    private Map<Integer, ClientHandler> connectedClients;

    // Broadcast manager for routing messages
    private BroadcastManager broadcastManager;

    public BirdSengerServer() {
        this.connectedClients = new ConcurrentHashMap<>();
        this.broadcastManager = new BroadcastManager(connectedClients);
    }

    /**
     * Start the server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;

            System.out.println("🚀 BirdSenger Server started on port " + PORT);
            System.out.println("⏳ Waiting for clients to connect...\n");

            // Accept client connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("🔌 New client connected: " + clientSocket.getInetAddress());

                    // Create handler for this client
                    ClientHandler clientHandler = new ClientHandler(
                            clientSocket,
                            this,
                            broadcastManager
                    );

                    // Start handler in new thread
                    new Thread(clientHandler).start();

                } catch (IOException e) {
                    if (running) {
                        System.err.println("❌ Error accepting client: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stop the server
     */
    public void stop() {
        running = false;

        try {
            // Disconnect all clients
            for (ClientHandler client : connectedClients.values()) {
                client.disconnect();
            }
            connectedClients.clear();

            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            System.out.println("🛑 Server stopped");

        } catch (IOException e) {
            System.err.println("❌ Error stopping server: " + e.getMessage());
        }
    }

    /**
     * Register a client after authentication
     * @param userId User ID
     * @param handler Client handler
     */
    public void registerClient(int userId, ClientHandler handler) {
        connectedClients.put(userId, handler);
        System.out.println("✅ User " + userId + " registered. Total clients: " + connectedClients.size());

        // Broadcast presence update to all clients
        broadcastManager.broadcastPresenceUpdate(userId, Protocol.ONLINE);
    }

    /**
     * Unregister a client (on disconnect)
     * @param userId User ID
     */
    public void unregisterClient(int userId) {
        connectedClients.remove(userId);
        System.out.println("👋 User " + userId + " disconnected. Total clients: " + connectedClients.size());

        // Broadcast presence update to all clients
        broadcastManager.broadcastPresenceUpdate(userId, Protocol.OFFLINE);
    }

    /**
     * Get number of connected clients
     * @return Client count
     */
    public int getClientCount() {
        return connectedClients.size();
    }

    /**
     * Check if a user is connected
     * @param userId User ID
     * @return true if connected
     */
    public boolean isUserConnected(int userId) {
        return connectedClients.containsKey(userId);
    }

    /**
     * Main method to run the server
     */
    public static void main(String[] args) {
        BirdSengerServer server = new BirdSengerServer();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Shutting down server...");
            server.stop();
        }));

        // Start server
        server.start();
    }
}