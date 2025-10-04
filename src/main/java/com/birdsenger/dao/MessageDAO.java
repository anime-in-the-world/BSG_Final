package com.birdsenger.dao;

import com.birdsenger.model.Message;
import com.birdsenger.model.MessageStatus;
import com.birdsenger.model.MessageType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Message entity
 * Handles all database operations related to messages
 */
public class MessageDAO {
    private final Connection connection;

    public MessageDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Save a new message to the database
     * @param message Message object to save
     * @return true if saved successfully
     */
    public boolean saveMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content, message_type, status, timestamp) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, message.getSenderId());
            pstmt.setInt(2, message.getReceiverId());
            pstmt.setString(3, message.getContent());
            pstmt.setString(4, message.getType().name());
            pstmt.setString(5, message.getStatus().name());
            pstmt.setString(6, message.getTimestamp().toString());

            pstmt.executeUpdate();
            System.out.println("✅ Message saved to database");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Failed to save message: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all messages between two users
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return List of messages ordered by timestamp
     */
    public List<Message> getMessagesBetweenUsers(int userId1, int userId2) {
        List<Message> messages = new ArrayList<>();
        String sql = """
            SELECT * FROM messages 
            WHERE (sender_id = ? AND receiver_id = ?) 
               OR (sender_id = ? AND receiver_id = ?)
            ORDER BY timestamp ASC
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId1);
            pstmt.setInt(2, userId2);
            pstmt.setInt(3, userId2);
            pstmt.setInt(4, userId1);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                messages.add(extractMessageFromResultSet(rs));
            }

            System.out.println("✅ Retrieved " + messages.size() + " messages");

        } catch (SQLException e) {
            System.err.println("❌ Failed to get messages: " + e.getMessage());
        }

        return messages;
    }

    /**
     * Get recent messages for a user (for conversation list)
     * @param userId User ID
     * @param limit Number of conversations to retrieve
     * @return List of recent messages
     */
    public List<Message> getRecentMessagesForUser(int userId, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = """
            SELECT * FROM messages 
            WHERE sender_id = ? OR receiver_id = ?
            ORDER BY timestamp DESC
            LIMIT ?
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, limit);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                messages.add(extractMessageFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to get recent messages: " + e.getMessage());
        }

        return messages;
    }

    /**
     * Update message status (e.g., from SENT to DELIVERED to READ)
     * @param messageId Message ID
     * @param status New status
     * @return true if updated successfully
     */
    public boolean updateMessageStatus(int messageId, MessageStatus status) {
        String sql = "UPDATE messages SET status = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, messageId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Failed to update message status: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get unread message count for a user
     * @param userId User ID
     * @return Number of unread messages
     */
    public int getUnreadMessageCount(int userId) {
        String sql = "SELECT COUNT(*) as count FROM messages WHERE receiver_id = ? AND status != 'READ'";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to get unread count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Mark all messages from a specific sender as read
     * @param receiverId Receiver user ID
     * @param senderId Sender user ID
     * @return true if updated successfully
     */
    public boolean markMessagesAsRead(int receiverId, int senderId) {
        String sql = "UPDATE messages SET status = 'READ' WHERE receiver_id = ? AND sender_id = ? AND status != 'READ'";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, receiverId);
            pstmt.setInt(2, senderId);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✅ Marked " + rowsAffected + " messages as read");
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Failed to mark messages as read: " + e.getMessage());
        }

        return false;
    }

    /**
     * Delete a message
     * @param messageId Message ID
     * @return true if deleted successfully
     */
    public boolean deleteMessage(int messageId) {
        String sql = "DELETE FROM messages WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, messageId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Failed to delete message: " + e.getMessage());
        }

        return false;
    }

    /**
     * Helper method to extract Message object from ResultSet
     */
    private Message extractMessageFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int senderId = rs.getInt("sender_id");
        int receiverId = rs.getInt("receiver_id");
        String content = rs.getString("content");
        String typeStr = rs.getString("message_type");
        String statusStr = rs.getString("status");
        String timestampStr = rs.getString("timestamp");

        MessageType type = MessageType.valueOf(typeStr);
        MessageStatus status = MessageStatus.valueOf(statusStr);
        LocalDateTime timestamp = LocalDateTime.parse(timestampStr);

        return new Message(id, senderId, receiverId, content, type, status, timestamp);
    }



    /**
     * Get list of users that current user has conversations with
     */
    public List<Integer> getConversationPartners(int userId) {
        List<Integer> partners = new ArrayList<>();
        String sql = """
        SELECT DISTINCT 
            CASE 
                WHEN sender_id = ? THEN receiver_id 
                ELSE sender_id 
            END as partner_id
        FROM messages 
        WHERE sender_id = ? OR receiver_id = ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                partners.add(rs.getInt("partner_id"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get conversation partners: " + e.getMessage());
        }
        return partners;
    }



}