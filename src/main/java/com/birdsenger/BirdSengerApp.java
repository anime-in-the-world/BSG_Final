package com.birdsenger;

import com.birdsenger.dao.DatabaseConnection;
import com.birdsenger.util.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Main application entry point for BirdSenger
 * Initializes database and launches the login screen
 */
public class BirdSengerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("🚀 Starting BirdSenger...");

        // Initialize database
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        dbConnection.initializeDatabase();

        // Set up SceneManager with primary stage
        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.setPrimaryStage(primaryStage);

        // Load the login/signup view
        URL fxmlLocation = getClass().getResource("/fxml/LoginSignup.fxml");
        if (fxmlLocation == null) {
            throw new RuntimeException("Cannot find FXML file: /fxml/LoginSignup.fxml");
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        Scene scene = new Scene(root, 1000, 700);

        // Load CSS
        URL cssLocation = getClass().getResource("/css/style.css");
        if (cssLocation != null) {
            scene.getStylesheets().add(cssLocation.toExternalForm());
        } else {
            System.err.println("⚠️ Warning: CSS file not found: /css/style.css");
        }

        primaryStage.setTitle("BirdSenger - Secure Messenger");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        System.out.println("✅ BirdSenger started successfully!");
    }

    @Override
    public void stop() throws Exception {
        System.out.println("🛑 Shutting down BirdSenger...");

        // Close database connection
        DatabaseConnection.getInstance().closeConnection();

        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}