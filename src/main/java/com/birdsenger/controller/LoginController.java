package com.birdsenger.controller;

import com.birdsenger.dao.UserDAO;
import com.birdsenger.model.User;
import com.birdsenger.util.SceneManager;
import com.birdsenger.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for Login and Signup screens
 * Handles user authentication and registration
 */
public class LoginController implements Initializable {

    // Theme & forms
    @FXML
    private ToggleButton themeSwitch;
    @FXML
    private Label themeLabel;
    @FXML
    private VBox loginForm;
    @FXML
    private VBox signupForm;

    // Login controls
    @FXML
    private TextField loginUsername;
    @FXML
    private PasswordField loginPassword;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink forgotPassword;
    @FXML
    private Hyperlink switchToSignup;
    @FXML
    private Label loginMessage;

    // Signup controls
    @FXML
    private TextField signupEmail;
    @FXML
    private TextField signupUsername;
    @FXML
    private PasswordField signupPassword;
    @FXML
    private PasswordField confirmPassword;
    @FXML
    private Button signupButton;
    @FXML
    private Hyperlink switchToLogin;
    @FXML
    private Label signupMessage;

    // DAOs
    private UserDAO userDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAO
        userDAO = new UserDAO();

        // Switch between forms
        if (switchToSignup != null) {
            switchToSignup.setOnAction(_e -> showSignup());
        }
        if (switchToLogin != null) {
            switchToLogin.setOnAction(_e -> showLogin());
        }

        // Theme toggle
        if (themeSwitch != null) {
            themeSwitch.setSelected(false); // start light
            themeSwitch.setOnAction(_ -> {
                toggleTheme(themeSwitch.isSelected());
                if (themeLabel != null)
                    themeLabel.setText(themeSwitch.isSelected() ? "Dark" : "Light");
            });
            themeSwitch.sceneProperty().addListener((_obs, _oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.getRoot().getStyleClass().remove("dark-theme");
                    if (!newScene.getRoot().getStyleClass().contains("light-theme")) {
                        newScene.getRoot().getStyleClass().add("light-theme");
                    }
                    if (themeLabel != null)
                        themeLabel.setText("Light");
                }
            });
        }

        // Buttons
        if (forgotPassword != null) {
            forgotPassword.setOnAction(e -> handleForgotPassword());
        }
        if (loginButton != null) {
            loginButton.setOnAction(e -> onLogin());
        }
        if (signupButton != null) {
            signupButton.setOnAction(e -> onSignup());
        }

        System.out.println("✅ LoginController initialized");
    }

    private void showSignup() {
        if (loginForm != null && signupForm != null) {
            loginForm.setVisible(false);
            loginForm.setManaged(false);
            signupForm.setVisible(true);
            signupForm.setManaged(true);
            fadeIn(signupForm);
            clearMessages();
        }
    }

    private void showLogin() {
        if (loginForm != null && signupForm != null) {
            signupForm.setVisible(false);
            signupForm.setManaged(false);
            loginForm.setVisible(true);
            loginForm.setManaged(true);
            fadeIn(loginForm);
            clearMessages();
        }
    }

    private void toggleTheme(boolean dark) {
        Scene scene = themeSwitch.getScene();
        if (scene == null)
            return;
        if (dark) {
            scene.getRoot().getStyleClass().remove("light-theme");
            if (!scene.getRoot().getStyleClass().contains("dark-theme")) {
                scene.getRoot().getStyleClass().add("dark-theme");
            }
        } else {
            scene.getRoot().getStyleClass().remove("dark-theme");
            if (!scene.getRoot().getStyleClass().contains("light-theme")) {
                scene.getRoot().getStyleClass().add("light-theme");
            }
        }
    }

    private void fadeIn(VBox node) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    // --- Actions ---
    private void onLogin() {
        clearMessages();
        String username = loginUsername.getText();
        String password = loginPassword.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            showMessage(loginMessage, "Please fill in all fields", "error");
            return;
        }

        // Validate credentials using UserDAO
        if (userDAO.validateCredentials(username, password)) {
            // Get user details
            User user = userDAO.getUserByUsernameOrEmail(username);

            if (user != null) {
                // Set current user in session
                SessionManager.getInstance().setCurrentUser(user);

                showMessage(loginMessage, "Login successful!", "success");
                System.out.println("✅ Login successful for: " + user.getUsername());

                // Navigate to MainChat (will create this next)
                try {
                    SceneManager.getInstance().switchScene("/fxml/MainChat.fxml", "BirdSenger - Chat");
                } catch (IOException e) {
                    System.err.println("❌ Failed to load MainChat: " + e.getMessage());
                    showMessage(loginMessage, "Failed to load chat screen", "error");
                }
            }
        } else {
            showMessage(loginMessage, "Invalid username/email or password", "error");
        }
    }

    private void onSignup() {
        clearMessages();
        String email = signupEmail.getText();
        String username = signupUsername.getText();
        String password = signupPassword.getText();
        String confirm = confirmPassword.getText();

        if (email.isBlank() || username.isBlank() || password.isBlank() || confirm.isBlank()) {
            showMessage(signupMessage, "Please fill in all fields", "error");
            return;
        }

        if (!password.equals(confirm)) {
            showMessage(signupMessage, "Passwords do not match", "error");
            return;
        }

        if (password.length() < 6) {
            showMessage(signupMessage, "Password must be at least 6 characters", "error");
            return;
        }

        // Check if user already exists
        if (userDAO.userExists(username) || userDAO.userExists(email)) {
            showMessage(signupMessage, "Username or email already exists", "error");
            return;
        }

        // Create user using UserDAO
        boolean ok = userDAO.createUser(username, email, password);
        if (ok) {
            showMessage(signupMessage, "Account created successfully!", "success");
            clearSignupFields();

            // Auto-switch to login after 1 second
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(this::showLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showMessage(signupMessage, "Failed to create account", "error");
        }
    }

    private void showMessage(Label label, String text, String type) {
        label.setText(text);
        if ("success".equals(type)) {
            label.setStyle("-fx-text-fill: #10b981;");
        } else {
            label.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    private void clearMessages() {
        if (loginMessage != null)
            loginMessage.setText("");
        if (signupMessage != null)
            signupMessage.setText("");
    }

    private void clearSignupFields() {
        signupUsername.clear();
        signupEmail.clear();
        signupPassword.clear();
        confirmPassword.clear();
    }

    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forgot Password");
        alert.setHeaderText("Password Recovery");
        alert.setContentText("Password recovery feature will be implemented in a future update.");
        alert.showAndWait();
    }
}