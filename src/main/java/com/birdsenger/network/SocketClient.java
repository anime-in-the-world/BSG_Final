package com.birdsenger.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;
import java.net.InetSocketAddress;

/**
 * Socket client for connecting to BirdSenger server
 * Handles TCP connection, sending and receiving messages
 */
public class SocketClient {
//    private static final String SERVER_HOST = "localhost";
    private static final String SERVER_HOST = "18.142.245.55";
    private static final int SERVER_PORT = 8080;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    private boolean connected;
    private Consumer<String> messageCallback;

    /**
     * Connect to the server
     * @return true if connected successfully
     */



//    public boolean connect() {
//        try {
//            socket = new Socket(SERVER_HOST, SERVER_PORT);
//            out = new PrintWriter(socket.getOutputStream(), true);
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            connected = true;
//
//            System.out.println("✅ Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);
//
//            // Start listener thread
//            startListening();
//
//            return true;
//
//        } catch (IOException e) {
//            System.err.println("❌ Failed to connect to server: " + e.getMessage());
//            connected = false;
//            return false;
//        }
//    }



    public boolean connect() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT), 5000); // 5 second timeout
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;

            System.out.println("✅ Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);

            // Start listener thread
            startListening();

            return true;

        } catch (IOException e) {
            System.err.println("❌ Failed to connect to server: " + e.getMessage());
            connected = false;
            return false;
        }
    }












    /**
     * Set callback for received messages
     * @param callback Function to handle received messages
     */
    public void setMessageCallback(Consumer<String> callback) {
        this.messageCallback = callback;
    }

    /**
     * Send message to server
     * @param message JSON message string
     * @return true if sent successfully
     */
    public boolean sendMessage(String message) {
        if (!connected || out == null) {
            System.err.println("❌ Cannot send message: Not connected to server");
            return false;
        }

        try {
            out.println(message);
            System.out.println("📤 Sent to server: " + message.substring(0, Math.min(50, message.length())) + "...");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to send message: " + e.getMessage());
            return false;
        }
    }

    /**
     * Start listening for incoming messages in a background thread
     */
    private void startListening() {
        listenerThread = new Thread(() -> {
            try {
                String message;
                while (connected && (message = in.readLine()) != null) {
                    final String receivedMessage = message;
                    System.out.println("📥 Received from server: " + receivedMessage.substring(0, Math.min(50, receivedMessage.length())) + "...");

                    // Call the message callback if set
                    if (messageCallback != null) {
                        messageCallback.accept(receivedMessage);
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    System.err.println("❌ Connection lost: " + e.getMessage());
                    connected = false;
                }
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.setName("SocketClient-Listener");
        listenerThread.start();
        System.out.println("✅ Started listening for messages");
    }

    /**
     * Disconnect from server
     */
    public void disconnect() {
        connected = false;

        try {
            if (listenerThread != null) {
                listenerThread.interrupt();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("✅ Disconnected from server");
        } catch (IOException e) {
            System.err.println("❌ Error during disconnect: " + e.getMessage());
        }
    }

    /**
     * Check if connected to server
     * @return true if connected
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    /**
     * Attempt to reconnect to server
     * @return true if reconnected successfully
     */
    public boolean reconnect() {
        System.out.println("🔄 Attempting to reconnect...");
        disconnect();
        try {
            Thread.sleep(1000); // Wait 1 second before reconnecting
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return connect();
    }

    /**
     * Get server host
     * @return Server hostname
     */
    public String getServerHost() {
        return SERVER_HOST;
    }

    /**
     * Get server port
     * @return Server port number
     */
    public int getServerPort() {
        return SERVER_PORT;
    }
}