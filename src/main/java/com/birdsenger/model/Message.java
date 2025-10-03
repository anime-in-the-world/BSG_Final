package com.birdsenger.model;

import java.time.LocalDateTime;

/**
 * Message entity representing a chat message
 * Maps to the 'messages' table in SQLite database
 */
public class Message {
    private int id;
    private int senderId;
    private int receiverId;
    private String content;
    private MessageType type;
    private MessageStatus status;
    private LocalDateTime timestamp;

    // Constructor for creating new message (before DB insert)
    public Message(int senderId, int receiverId, String content, MessageType type) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
        this.status = MessageStatus.SENT;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor for loading from database (with ID)
    public Message(int id, int senderId, int receiverId, String content,
                   MessageType type, MessageStatus status, LocalDateTime timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", timestamp=" + timestamp +
                '}';
    }
}