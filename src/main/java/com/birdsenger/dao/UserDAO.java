package com.birdsenger.dao;

import com.birdsenger.model.PresenceStatus;
import com.birdsenger.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Data Access Object for User entity
 * Handles all database operations related to users
 */
public class UserDAO {
    private final Connection connection;

    public UserDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Create a new user in the database
     * @param username Unique username
     * @param email Unique email
     * @param password Plain text password (will be hashed)
     * @return true if user created successfully, false otherwise
     */
    public boolean createUser(String username, String email, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String currentTime = LocalDateTime.now().toString();

        String sql = "INSERT INTO users (username, email, password_hash, created_at, status) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, hashedPassword);
            pstmt.setString(4, currentTime);
            pstmt.setString(5, PresenceStatus.OFFLINE.name());

            pstmt.executeUpdate();
            System.out.println("✅ User created: " + username);
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Failed to create user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate user credentials for login
     * @param usernameOrEmail Username or email
     * @param password Plain text password
     * @return true if credentials are valid, false otherwise
     */
    public boolean validateCredentials(String usernameOrEmail, String password) {
        String sql = "SELECT password_hash FROM users WHERE username = ? OR email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, usernameOrEmail);
            pstmt.setString(2, usernameOrEmail);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password_hash");
                return BCrypt.checkpw(password, hashedPassword);
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to validate credentials: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get user by username or email
     * @param usernameOrEmail Username or email
     * @return User object or null if not found
     */
    public User getUserByUsernameOrEmail(String usernameOrEmail) {
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, usernameOrEmail);
            pstmt.setString(2, usernameOrEmail);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to get user: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get user by ID
     * @param userId User ID
     * @return User object or null if not found
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to get user by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Check if username or email already exists
     * @param usernameOrEmail Username or email to check
     * @return true if exists, false otherwise
     */
    public boolean userExists(String usernameOrEmail) {
        String sql = "SELECT id FROM users WHERE username = ? OR email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, usernameOrEmail);
            pstmt.setString(2, usernameOrEmail);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("❌ Failed to check user existence: " + e.getMessage());
        }

        return false;
    }

    /**
     * Update user's online status
     * @param userId User ID
     * @param status New presence status
     * @return true if updated successfully
     */
    public boolean updateUserStatus(int userId, PresenceStatus status) {
        String sql = "UPDATE users SET status = ?, last_seen = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, LocalDateTime.now().toString());
            pstmt.setInt(3, userId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Failed to update user status: " + e.getMessage());
        }

        return false;
    }

    /**
     * Update user profile (username, bio, avatar)
     * @param user User object with updated information
     * @return true if updated successfully
     */
    public boolean updateUserProfile(User user) {
        String sql = "UPDATE users SET username = ?, bio = ?, avatar_path = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getBio());
            pstmt.setString(3, user.getAvatarPath());
            pstmt.setInt(4, user.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Failed to update user profile: " + e.getMessage());
        }

        return false;
    }

    /**
     * Helper method to extract User object from ResultSet
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String avatarPath = rs.getString("avatar_path");
        String bio = rs.getString("bio");
        String statusStr = rs.getString("status");
        String createdAtStr = rs.getString("created_at");
        String lastSeenStr = rs.getString("last_seen");

        PresenceStatus status = PresenceStatus.valueOf(statusStr);
        LocalDateTime createdAt = LocalDateTime.parse(createdAtStr);
        LocalDateTime lastSeen = lastSeenStr != null ? LocalDateTime.parse(lastSeenStr) : null;

        return new User(id, username, email, passwordHash, avatarPath, bio, status, createdAt, lastSeen);
    }
}