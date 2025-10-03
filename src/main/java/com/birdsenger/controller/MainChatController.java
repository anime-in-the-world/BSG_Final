package com.birdsenger.controller;

import com.birdsenger.dao.MessageDAO;
import com.birdsenger.dao.UserDAO;
import com.birdsenger.model.Message;
import com.birdsenger.model.MessageType;
import com.birdsenger.model.User;
import com.birdsenger.util.SceneManager;
import com.birdsenger.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller for the main chat interface
 * Handles message display, sending, and conversation management
 */
public class MainChatController implements Initializable {

    // Header
    @FXML
    private Label currentUserLabel;
    @FXML
    private Button logoutButton;

    // Conversations panel
    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> conversationsList;
    @FXML
    private Button newChatButton;

    // Chat area
    @FXML
    private Label chatWithLabel;
    @FXML
    private Label chatStatusLabel;
    @FXML
    private ScrollPane messagesScrollPane;
    @FXML
    private VBox messagesContainer;
    @FXML
    private TextArea messageInput;
    @FXML
    private Button sendButton;

    // Data
    private User currentUser;
    private UserDAO userDAO;
    private MessageDAO messageDAO;
    private User selectedChatUser;
    private Map<String, User> conversationsMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ MainChatController initializing...");

        // Initialize DAOs
        userDAO = new UserDAO();
        messageDAO = new MessageDAO();
        conversationsMap = new HashMap<>();

        // Get current user from session
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showError("No user logged in!");
            return;
        }

        // Display current user
        currentUserLabel.setText(currentUser.getUsername());
        System.out.println("✅ Current user: " + currentUser.getUsername());

        // Setup event handlers
        setupEventHandlers();

        // Load conversations (for now, just show a simple list)
        loadConversations();

        System.out.println("✅ MainChatController initialized successfully");
    }

    private void setupEventHandlers() {
        // Logout button
        logoutButton.setOnAction(e -> handleLogout());

        // New chat button
        newChatButton.setOnAction(e -> handleNewChat());

        // Send button
        sendButton.setOnAction(e -> handleSendMessage());

        // Enter key to send message
        messageInput.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER") && !e.isShiftDown()) {
                e.consume();
                handleSendMessage();
            }
        });

        // Conversation selection
        conversationsList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        handleConversationSelected(newValue);
                    }
                }
        );

        // Auto-scroll to bottom when new messages added
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            messagesScrollPane.setVvalue(1.0);
        });
    }

    private void loadConversations() {
        // For MVP: Create a simple test conversation
        // In Phase 2, we'll load actual friend list

        conversationsList.getItems().clear();

        // Add a placeholder instruction
        conversationsList.getItems().add("Click 'New Chat' to start");

        System.out.println("✅ Conversations loaded");
    }

    private void handleNewChat() {
        // Dialog to enter username to chat with
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Chat");
        dialog.setHeaderText("Start a new conversation");
        dialog.setContentText("Enter username or email:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(usernameOrEmail -> {
            if (usernameOrEmail.equals(currentUser.getUsername()) ||
                    usernameOrEmail.equals(currentUser.getEmail())) {
                showError("You cannot chat with yourself!");
                return;
            }

            // Look up user
            User chatUser = userDAO.getUserByUsernameOrEmail(usernameOrEmail);

            if (chatUser == null) {
                showError("User not found: " + usernameOrEmail);
                return;
            }

            // Add to conversations list if not already there
            if (!conversationsMap.containsKey(chatUser.getUsername())) {
                conversationsMap.put(chatUser.getUsername(), chatUser);
                conversationsList.getItems().remove("Click 'New Chat' to start");
                conversationsList.getItems().add(chatUser.getUsername());
                System.out.println("✅ Added conversation with: " + chatUser.getUsername());
            }

            // Select this conversation
            conversationsList.getSelectionModel().select(chatUser.getUsername());
        });
    }

    private void handleConversationSelected(String username) {
        if (username.equals("Click 'New Chat' to start")) {
            return;
        }

        selectedChatUser = conversationsMap.get(username);

        if (selectedChatUser == null) {
            return;
        }

        // Update chat header
        chatWithLabel.setText(selectedChatUser.getUsername());
        chatStatusLabel.setText("Online"); // TODO: Real presence status in Phase 2

        // Load messages
        loadMessages();

        System.out.println("✅ Selected conversation with: " + selectedChatUser.getUsername());
    }

    private void loadMessages() {
        if (selectedChatUser == null) {
            return;
        }

        messagesContainer.getChildren().clear();

        // Get messages from database
        List<Message> messages = messageDAO.getMessagesBetweenUsers(
                currentUser.getId(),
                selectedChatUser.getId()
        );

        // Display each message
        for (Message message : messages) {
            displayMessage(message);
        }

        System.out.println("✅ Loaded " + messages.size() + " messages");
    }

    private void handleSendMessage() {
        String content = messageInput.getText().trim();

        if (content.isEmpty()) {
            return;
        }

        if (selectedChatUser == null) {
            showError("Please select a conversation first!");
            return;
        }

        // Create message object
        Message message = new Message(
                currentUser.getId(),
                selectedChatUser.getId(),
                content,
                MessageType.TEXT
        );

        // Save to database
        boolean saved = messageDAO.saveMessage(message);

        if (saved) {
            // Display message
            displayMessage(message);

            // Clear input
            messageInput.clear();

            System.out.println("✅ Message sent to: " + selectedChatUser.getUsername());
        } else {
            showError("Failed to send message!");
        }
    }

    private void displayMessage(Message message) {
        boolean isSent = (message.getSenderId() == currentUser.getId());

        // Create message bubble
        VBox messageBubble = createMessageBubble(message, isSent);

        // Create container to align message
        HBox messageContainer = new HBox();
        messageContainer.setAlignment(isSent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageContainer.getChildren().add(messageBubble);
        messageContainer.setPadding(new Insets(5, 0, 5, 0));

        // Add to messages container
        messagesContainer.getChildren().add(messageContainer);
    }

    private VBox createMessageBubble(Message message, boolean isSent) {
        VBox bubble = new VBox(5);
        bubble.getStyleClass().add("message-bubble");
        bubble.getStyleClass().add(isSent ? "message-sent" : "message-received");
        bubble.setMaxWidth(500);

        // Message text
        Text messageText = new Text(message.getContent());
        messageText.getStyleClass().add(isSent ? "message-text-sent" : "message-text-received");
        messageText.setWrappingWidth(480);

        // Timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String timeStr = message.getTimestamp().format(formatter);
        Label timeLabel = new Label(timeStr);
        timeLabel.getStyleClass().add(isSent ? "message-time" : "message-time-received");

        bubble.getChildren().addAll(messageText, timeLabel);

        return bubble;
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will need to login again to use BirdSenger.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Clear session
            SessionManager.getInstance().clearSession();

            // Navigate back to login
            try {
                SceneManager.getInstance().switchScene("/fxml/LoginSignup.fxml", "BirdSenger - Login");
                System.out.println("✅ Logged out successfully");
            } catch (IOException e) {
                System.err.println("❌ Failed to load login screen: " + e.getMessage());
                showError("Failed to logout!");
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}