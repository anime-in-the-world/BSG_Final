package com.birdsenger.util;

import com.birdsenger.model.User;

/**
 * Singleton class to manage user session
 * Stores the currently logged-in user throughout the application
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    // Private constructor for Singleton pattern
    private SessionManager() {
        this.currentUser = null;
    }

    /**
     * Get the singleton instance of SessionManager
     * @return SessionManager instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Set the current logged-in user
     * @param user User object
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("✅ Session started for user: " + user.getUsername());
    }

    /**
     * Get the current logged-in user
     * @return User object or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is currently logged in
     * @return true if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Clear the current session (logout)
     */
    public void clearSession() {
        if (currentUser != null) {
            System.out.println("✅ Session cleared for user: " + currentUser.getUsername());
            this.currentUser = null;
        }
    }

    /**
     * Get current user's ID
     * @return User ID or -1 if not logged in
     */
    public int getCurrentUserId() {
        return isLoggedIn() ? currentUser.getId() : -1;
    }

    /**
     * Get current user's username
     * @return Username or null if not logged in
     */
    public String getCurrentUsername() {
        return isLoggedIn() ? currentUser.getUsername() : null;
    }
}