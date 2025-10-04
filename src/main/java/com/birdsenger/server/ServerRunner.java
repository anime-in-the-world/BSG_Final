package com.birdsenger.server;

import com.birdsenger.server.BirdSengerServer;

public class ServerRunner {
    public static void main(String[] args) {
        BirdSengerServer server = new BirdSengerServer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Shutting down server...");
            server.stop();
        }));

        server.start();
    }
}