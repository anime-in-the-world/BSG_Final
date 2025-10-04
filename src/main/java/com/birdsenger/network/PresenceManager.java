package com.birdsenger.network;

import com.birdsenger.dao.UserDAO;
import com.birdsenger.model.PresenceStatus;
import com.birdsenger.model.User;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Manages user presence status and heartbeat to server
 * Sends periodic heartbeat to indicate user is online
 */
public class PresenceManager {
    private static final int HEARTBEAT_INTERVAL = 30000; // 30 seconds

    private SocketClient socketClient;
    private UserDAO userDAO;
    private User currentUser;
    private Timer heartbeatTimer;
    private PresenceStatus currentStatus;

    public PresenceManager(SocketClient socketClient, User currentUser) {
        this.socketClient = socketClient;
        this.currentUser = currentUser;
        this.userDAO = new UserDAO();
        this.currentStatus = PresenceStatus.ONLINE;
    }

    /**
     * Start sending heartbeat to server
     */
    public void startHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
        }

        // Update user status to ONLINE in database
        userDAO.updateUserStatus(currentUser.getId(), PresenceStatus.ONLINE);

        // Send initial presence update
        sendPresenceUpdate(PresenceStatus.ONLINE);

        // Schedule periodic heartbeat
        heartbeatTimer = new Timer("PresenceHeartbeat", true);
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendHeartbeat();
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL);

        System.out.println("💚 Started heartbeat for user: " + currentUser.getUsername());
    }

    /**
     * Stop sending heartbeat
     */
    public void stopHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
            heartbeatTimer = null;
        }

        // Update user status to OFFLINE
        userDAO.updateUserStatus(currentUser.getId(), PresenceStatus.OFFLINE);
        sendPresenceUpdate(PresenceStatus.OFFLINE);

        System.out.println("💔 Stopped heartbeat for user: " + currentUser.getUsername());
    }

    /**
     * Send heartbeat to server
     */
    private void sendHeartbeat() {
        if (socketClient != null && socketClient.isConnected()) {
            String presenceMessage = Protocol.createPresenceMessage(
                    currentUser.getId(),
                    currentStatus.name()
            );
            socketClient.sendMessage(presenceMessage);
            System.out.println("💓 Heartbeat sent");
        } else {
            System.err.println("⚠️ Cannot send heartbeat: Not connected to server");
        }
    }

    /**
     * Update user presence status
     * @param status New presence status
     */
    public void updateStatus(PresenceStatus status) {
        this.currentStatus = status;

        // Update in database
        userDAO.updateUserStatus(currentUser.getId(), status);

        // Notify server
        sendPresenceUpdate(status);

        System.out.println("✅ Status updated to: " + status);
    }

    /**
     * Send presence update to server
     */
    private void sendPresenceUpdate(PresenceStatus status) {
        if (socketClient != null && socketClient.isConnected()) {
            String presenceMessage = Protocol.createPresenceMessage(
                    currentUser.getId(),
                    status.name()
            );
            socketClient.sendMessage(presenceMessage);
        }
    }

    /**
     * Get current presence status
     * @return Current status
     */
    public PresenceStatus getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Mark user as away (inactive)
     */
    public void setAway() {
        updateStatus(PresenceStatus.AWAY);
    }

    /**
     * Mark user as online (active)
     */
    public void setOnline() {
        updateStatus(PresenceStatus.ONLINE);
    }

    /**
     * Mark user as do not disturb
     */
    public void setDoNotDisturb() {
        updateStatus(PresenceStatus.DO_NOT_DISTURB);
    }
}