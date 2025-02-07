package com.myapp.noteapp.model;

public class Note {
    private long id;
    private String title;
    private String content;
    private String timestamp;
    private boolean pinned;
    private long userId;

    public Note(long id, String title, String content, String timestamp, boolean pinned, long userId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.pinned = pinned;
        this.userId = userId;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
} 