package com.birdsenger.controller;

import com.birdsenger.dao.MessageDAO;
import com.birdsenger.dao.UserDAO;
import com.birdsenger.model.Message;
import com.birdsenger.model.MessageType;
import com.birdsenger.model.User;
import com.birdsenger.network.*;
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


/** Notificaiton */
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.stage.Popup;


/**
 * Controller for the main chat interface - NOW WITH REAL-TIME MESSAGING!
 * Handles message display, sending, and conversation management
 */
public class MainChatController implements Initializable {

    private Map<String, Integer> unreadCounts; // username -> unread count

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

    // Network components (NEW!)
    private SocketClient socketClient;
    private MessageHandler messageHandler;
    private PresenceManager presenceManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ MainChatController initializing...");

        // Initialize DAOs
        userDAO = new UserDAO();
        messageDAO = new MessageDAO();
        conversationsMap = new HashMap<>();
        unreadCounts = new HashMap<>();

        // Get current user from session
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showError("No user logged in!");
            return;
        }

        // Display current user
        currentUserLabel.setText(currentUser.getUsername());
        System.out.println("✅ Current user: " + currentUser.getUsername());


        // Custom cell factory for conversation list with unread indicators
        conversationsList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String username, boolean empty) {
                super.updateItem(username, empty);
                if (empty || username == null || username.equals("Click 'New Chat' to start")) {
                    setText(username);
                    setStyle("");
                } else {
                    int unreadCount = unreadCounts.getOrDefault(username, 0);
                    if (unreadCount > 0) {
                        setText(username + " (" + unreadCount + ")");
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #111827;");
                    } else {
                        setText(username);
                        setStyle("-fx-font-weight: normal; -fx-text-fill: #6b7280;");
                    }
                }
            }
        });


        // Setup event handlers
        setupEventHandlers();

        // Load conversations
        loadConversations();

        // Initialize network components (NEW!)
        initializeNetworking();

        System.out.println("✅ MainChatController initialized successfully");
    }

    /**
     * Initialize networking - Connect to server
     */

    /*
    private void initializeNetworking() {
        try {
            // Create socket client
            socketClient = new SocketClient();

            // Create message handler
            messageHandler = new MessageHandler();

            // Set up callbacks for incoming messages
            messageHandler.setOnNewMessageReceived(this::handleIncomingMessage);
            messageHandler.setOnUserStatusChanged(this::handleUserStatusChange);
            messageHandler.setOnError(this::handleNetworkError);

            // Set socket client callback to route to message handler
            socketClient.setMessageCallback(jsonMessage -> {
                messageHandler.handleIncomingMessage(jsonMessage);
            });

            // Connect to server
            boolean connected = socketClient.connect();

            if (connected) {
                System.out.println("🌐 Connected to server!");

                // Send authentication message
                String authMessage = Protocol.createAuthMessage(
                        currentUser.getId(),
                        currentUser.getUsername()
                );
                socketClient.sendMessage(authMessage);

                // Start presence manager
                presenceManager = new PresenceManager(socketClient, currentUser);
                presenceManager.startHeartbeat();

                System.out.println("✅ Networking initialized successfully");
            } else {
                showWarning("Could not connect to server. Messages will be local only.");
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize networking: " + e.getMessage());
            e.printStackTrace();
            showWarning("Running in offline mode. Start the server to enable real-time messaging.");
        }
    }

     */


    /**
     * Initialize networking - Connect to server (in background thread)
     */
    private void initializeNetworking() {
        // Run connection in background thread to avoid blocking UI
        new Thread(() -> {
            try {
                // Create socket client
                socketClient = new SocketClient();

                // Create message handler
                messageHandler = new MessageHandler();

                // Set up callbacks for incoming messages
                messageHandler.setOnNewMessageReceived(this::handleIncomingMessage);
                messageHandler.setOnUserStatusChanged(this::handleUserStatusChange);
                messageHandler.setOnError(this::handleNetworkError);

                // Set socket client callback to route to message handler
                socketClient.setMessageCallback(jsonMessage -> {
                    messageHandler.handleIncomingMessage(jsonMessage);
                });

                // Connect to server
                boolean connected = socketClient.connect();

                if (connected) {
                    System.out.println("🌐 Connected to server!");

                    // Send authentication message
                    String authMessage = Protocol.createAuthMessage(
                            currentUser.getId(),
                            currentUser.getUsername()
                    );
                    socketClient.sendMessage(authMessage);

                    // Start presence manager
                    presenceManager = new PresenceManager(socketClient, currentUser);
                    presenceManager.startHeartbeat();

                    System.out.println("✅ Networking initialized successfully");
                } else {
                    Platform.runLater(() -> {
                        showWarning("Could not connect to server. Messages will be local only.");
                    });
                }

            } catch (Exception e) {
                System.err.println("❌ Failed to initialize networking: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showWarning("Running in offline mode. Start the server to enable real-time messaging.");
                });
            }
        }).start();
    }












    /**
     * Handle incoming message from another user (REAL-TIME!)
     */
    private void handleIncomingMessage(Message message) {
        Platform.runLater(() -> {
            // Get the sender
            User sender = userDAO.getUserById(message.getSenderId());
            if (sender == null) {
                System.err.println("Could not find sender with ID: " + message.getSenderId());
                return;
            }

            System.out.println("Received message from " + sender.getUsername() + ": " + message.getContent());

            // Check if conversation already exists in sidebar
            boolean conversationExists = conversationsMap.containsKey(sender.getUsername());

            if (!conversationExists) {
                // Add new conversation to sidebar
                conversationsMap.put(sender.getUsername(), sender);
                conversationsList.getItems().remove("Click 'New Chat' to start");
                conversationsList.getItems().add(0, sender.getUsername());
                System.out.println("Added new conversation: " + sender.getUsername());
            }

            // If this chat is currently open, display the message
            if (selectedChatUser != null && message.getSenderId() == selectedChatUser.getId()) {
                displayMessage(message);
                System.out.println("Real-time message displayed from: " + sender.getUsername());
                // Don't move to top - keep current position since it's active
            } else {
                // Chat not open - move to top, increment unread count and show notification
                if (conversationExists) {
                    // Move to top only if not currently active
                    conversationsList.getItems().remove(sender.getUsername());
                    conversationsList.getItems().add(0, sender.getUsername());
                }

                int currentUnread = unreadCounts.getOrDefault(sender.getUsername(), 0);
                unreadCounts.put(sender.getUsername(), currentUnread + 1);
                System.out.println("New message in sidebar from: " + sender.getUsername() + " (unread: " + (currentUnread + 1) + ")");

                // Show notification toast
                showNotificationToast(sender.getUsername(), message.getContent());
            }

            // Refresh the list to update styling
            conversationsList.refresh();
        });
    }







    /**
     * Handle user status change (online/offline)
     */
    private void handleUserStatusChange(String statusUpdate) {
        Platform.runLater(() -> {
            String[] parts = statusUpdate.split(":");
            int userId = Integer.parseInt(parts[0]);
            String status = parts[1];

            // Update status label if it's the current chat user
            if (selectedChatUser != null && selectedChatUser.getId() == userId) {
                chatStatusLabel.setText(status);
                System.out.println("💚 Status updated: " + selectedChatUser.getUsername() + " is " + status);
            }
        });
    }

    /**
     * Handle network error
     */
    private void handleNetworkError(String error) {
        Platform.runLater(() -> {
            System.err.println("❌ Network error: " + error);
        });
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
        conversationsList.getItems().clear();

        // Get all users who have messages with current user
        List<Integer> partnerIds = messageDAO.getConversationPartners(currentUser.getId());

        if (partnerIds.isEmpty()) {
            conversationsList.getItems().add("Click 'New Chat' to start");
        } else {
            for (int partnerId : partnerIds) {
                User partner = userDAO.getUserById(partnerId);
                if (partner != null) {
                    conversationsMap.put(partner.getUsername(), partner);
                    conversationsList.getItems().add(partner.getUsername());
                }
            }
        }

        System.out.println("👥 Loaded " + partnerIds.size() + " conversations");  // <-- Notice the emoji
    }




    private void handleNewChat() {
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

            User chatUser = userDAO.getUserByUsernameOrEmail(usernameOrEmail);

            if (chatUser == null) {
                showError("User not found: " + usernameOrEmail);
                return;
            }

            if (!conversationsMap.containsKey(chatUser.getUsername())) {
                conversationsMap.put(chatUser.getUsername(), chatUser);
                conversationsList.getItems().remove("Click 'New Chat' to start");
                conversationsList.getItems().add(chatUser.getUsername());
                System.out.println("✅ Added conversation with: " + chatUser.getUsername());
            }

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

        chatWithLabel.setText(selectedChatUser.getUsername());
        chatStatusLabel.setText("Online"); // Will be updated by presence

        // Clear unread count when opening conversation
        unreadCounts.put(username, 0);
        conversationsList.refresh();

        loadMessages();

        System.out.println("Selected conversation with: " + selectedChatUser.getUsername());
    }






    private void loadMessages() {
        if (selectedChatUser == null) {
            return;
        }

        messagesContainer.getChildren().clear();

        List<Message> messages = messageDAO.getMessagesBetweenUsers(
                currentUser.getId(),
                selectedChatUser.getId()
        );

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
            // Display message locally
            displayMessage(message);

            // Send through server (NEW!)
            if (socketClient != null && socketClient.isConnected()) {
                String chatMessage = Protocol.createChatMessage(
                        currentUser.getId(),
                        selectedChatUser.getId(),
                        content
                );
                socketClient.sendMessage(chatMessage);
                System.out.println("⚡ Message sent via server to: " + selectedChatUser.getUsername());
            } else {
                System.out.println("⚠️ Sent locally only (server not connected)");
            }

            // Clear input
            messageInput.clear();
        } else {
            showError("Failed to send message!");
        }
    }

    private void displayMessage(Message message) {
        boolean isSent = (message.getSenderId() == currentUser.getId());

        VBox messageBubble = createMessageBubble(message, isSent);

        HBox messageContainer = new HBox();
        messageContainer.setAlignment(isSent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageContainer.getChildren().add(messageBubble);
        messageContainer.setPadding(new Insets(5, 0, 5, 0));

        messagesContainer.getChildren().add(messageContainer);
    }

    private VBox createMessageBubble(Message message, boolean isSent) {
        VBox bubble = new VBox(5);
        bubble.getStyleClass().add("message-bubble");
        bubble.getStyleClass().add(isSent ? "message-sent" : "message-received");
        bubble.setMaxWidth(500);

        Text messageText = new Text(message.getContent());
        messageText.getStyleClass().add(isSent ? "message-text-sent" : "message-text-received");
        messageText.setWrappingWidth(480);

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

            // Disconnect from server (NEW!)
            if (presenceManager != null) {
                presenceManager.stopHeartbeat();
            }
            if (socketClient != null) {
                String logoutMessage = Protocol.createLogoutMessage(currentUser.getId());
                socketClient.sendMessage(logoutMessage);
                socketClient.disconnect();
            }

            // Clear session
            SessionManager.getInstance().clearSession();

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

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); // Non-blocking
    }



    /** Method for notification */
    /**
     * Show notification toast at top of screen
     */
    /**
     * Show notification toast at top of screen
     */
    private void showNotificationToast(String sender, String message) {
        // Create notification content
        VBox notificationBox = new VBox(5);
        notificationBox.setAlignment(Pos.CENTER);
        notificationBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15 25;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);" +
                        "-fx-border-color: #f97316;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;"
        );

        Label senderLabel = new Label("New message from " + sender);
        senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #111827;");

        String preview = message.length() > 50 ? message.substring(0, 50) + "..." : message;
        Label messageLabel = new Label(preview);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        notificationBox.getChildren().addAll(senderLabel, messageLabel);

        // Wrap in StackPane to get proper sizing
        StackPane wrapper = new StackPane(notificationBox);
        wrapper.setPrefWidth(350);
        wrapper.setMaxWidth(350);

        // Create popup
        Popup popup = new Popup();
        popup.getContent().add(wrapper);
        popup.setAutoHide(false);

        // Get the main stage
        javafx.stage.Stage stage = (javafx.stage.Stage) conversationsList.getScene().getWindow();

        // Show popup first (hidden) to calculate size
        popup.setOpacity(0);
        popup.show(stage);

        // Calculate center position after layout
        Platform.runLater(() -> {
            double stageX = stage.getX();
            double stageY = stage.getY();
            double stageWidth = stage.getWidth();
            double popupWidth = wrapper.getWidth();

            // Center horizontally, 60px from top
            double x = stageX + (stageWidth / 2) - (popupWidth / 2);
            double y = stageY + 60;

            popup.setX(x);
            popup.setY(y);
            popup.setOpacity(1);

            // Fade in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), wrapper);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            // Auto-dismiss after 2 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), wrapper);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(ev -> popup.hide());
                fadeOut.play();
            });
            pause.play();
        });
    }




}