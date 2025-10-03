package com.birdsenger.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Singleton class to manage scene navigation
 * Handles loading FXML files and switching between views
 */
public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;

    // Private constructor for Singleton pattern
    private SceneManager() {
    }

    /**
     * Get the singleton instance of SceneManager
     * @return SceneManager instance
     */
    public static SceneManager getInstance() {
        if (instance == null) {
            synchronized (SceneManager.class) {
                if (instance == null) {
                    instance = new SceneManager();
                }
            }
        }
        return instance;
    }

    /**
     * Set the primary stage
     * @param stage Primary stage from Application.start()
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Get the primary stage
     * @return Primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Switch to a new scene
     * @param fxmlPath Path to FXML file (e.g., "/fxml/MainChat.fxml")
     * @param title Window title
     * @throws IOException if FXML file cannot be loaded
     */
    public void switchScene(String fxmlPath, String title) throws IOException {
        URL fxmlLocation = getClass().getResource(fxmlPath);

        if (fxmlLocation == null) {
            throw new IOException("Cannot find FXML file: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        Scene scene = new Scene(root);

        // Apply CSS if available
        URL cssLocation = getClass().getResource("/css/style.css");
        if (cssLocation != null) {
            scene.getStylesheets().add(cssLocation.toExternalForm());
        }

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);

        System.out.println("✅ Switched to scene: " + fxmlPath);
    }

    /**
     * Load an FXML file and return the controller
     * @param fxmlPath Path to FXML file
     * @param <T> Controller type
     * @return Controller instance
     * @throws IOException if FXML file cannot be loaded
     */
    public <T> T loadFXMLWithController(String fxmlPath) throws IOException {
        URL fxmlLocation = getClass().getResource(fxmlPath);

        if (fxmlLocation == null) {
            throw new IOException("Cannot find FXML file: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        loader.load();

        return loader.getController();
    }

    /**
     * Switch scene with custom width and height
     * @param fxmlPath Path to FXML file
     * @param title Window title
     * @param width Scene width
     * @param height Scene height
     * @throws IOException if FXML file cannot be loaded
     */
    public void switchScene(String fxmlPath, String title, double width, double height) throws IOException {
        URL fxmlLocation = getClass().getResource(fxmlPath);

        if (fxmlLocation == null) {
            throw new IOException("Cannot find FXML file: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        Scene scene = new Scene(root, width, height);

        // Apply CSS if available
        URL cssLocation = getClass().getResource("/css/style.css");
        if (cssLocation != null) {
            scene.getStylesheets().add(cssLocation.toExternalForm());
        }

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);

        System.out.println("✅ Switched to scene: " + fxmlPath + " (" + width + "x" + height + ")");
    }
}