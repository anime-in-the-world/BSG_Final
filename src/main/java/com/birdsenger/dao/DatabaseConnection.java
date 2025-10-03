package com.birdsenger.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton class to manage SQLite database connection
 * Ensures only one connection instance exists throughout the application
 */
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:birdsenger.db";
    private static DatabaseConnection instance;
    private Connection connection;

    // Private constructor for Singleton pattern
    private DatabaseConnection() {
        try {
            this.connection = DriverManager.getConnection(DB_URL);
            System.out.println("✅ Database connection established");
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the singleton instance of DatabaseConnection
     * @return DatabaseConnection instance
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Get the database connection
     * @return Connection object
     */
    public Connection getConnection() {
        try {
            // Check if connection is closed and reconnect if needed
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("🔄 Database connection re-established");
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to get connection: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Initialize all database tables
     * Creates tables if they don't exist
     */
    public void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {

            // 1. USERS TABLE
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    avatar_path TEXT,
                    bio TEXT,
                    status TEXT DEFAULT 'OFFLINE',
                    created_at TEXT NOT NULL,
                    last_seen TEXT
                )
                """;
            stmt.execute(createUsersTable);

            // 2. MESSAGES TABLE
            String createMessagesTable = """
                CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sender_id INTEGER NOT NULL,
                    receiver_id INTEGER NOT NULL,
                    content TEXT NOT NULL,
                    message_type TEXT DEFAULT 'TEXT',
                    status TEXT DEFAULT 'SENT',
                    timestamp TEXT NOT NULL,
                    FOREIGN KEY (sender_id) REFERENCES users(id),
                    FOREIGN KEY (receiver_id) REFERENCES users(id)
                )
                """;
            stmt.execute(createMessagesTable);

            // 3. FRIENDS TABLE
            String createFriendsTable = """
                CREATE TABLE IF NOT EXISTS friends (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user1_id INTEGER NOT NULL,
                    user2_id INTEGER NOT NULL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (user1_id) REFERENCES users(id),
                    FOREIGN KEY (user2_id) REFERENCES users(id),
                    UNIQUE(user1_id, user2_id)
                )
                """;
            stmt.execute(createFriendsTable);

            // Create indexes for performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages(sender_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_receiver ON messages(receiver_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_timestamp ON messages(timestamp)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_friends_user1 ON friends(user1_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_friends_user2 ON friends(user2_id)");

            System.out.println("✅ Database schema initialized successfully");

        } catch (SQLException e) {
            System.err.println("❌ Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Close the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to close connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}