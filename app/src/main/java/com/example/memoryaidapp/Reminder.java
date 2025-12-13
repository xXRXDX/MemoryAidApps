package com.example.memoryaidapp;

public class Reminder {

    private String id;
    private String title;
    private String description;
    private String priority;
    private long scheduledTimeMillis;
    private long durationMillis;
    private int earnedXp;
    private boolean photoTaken;
    private boolean completed;

    // REQUIRED empty constructor for Firestore / serialization
    public Reminder() {
    }

    public Reminder(String id,
                    String title,
                    String description,
                    String priority,
                    long scheduledTimeMillis) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.scheduledTimeMillis = scheduledTimeMillis;
        this.durationMillis = 0L;
        this.earnedXp = 0;
        this.photoTaken = false;
        this.completed = false;
    }

    // -------- GETTERS & SETTERS --------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public long getScheduledTimeMillis() {
        return scheduledTimeMillis;
    }

    public void setScheduledTimeMillis(long scheduledTimeMillis) {
        this.scheduledTimeMillis = scheduledTimeMillis;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public int getEarnedXp() {
        return earnedXp;
    }

    public void setEarnedXp(int earnedXp) {
        this.earnedXp = earnedXp;
    }

    public boolean isPhotoTaken() {
        return photoTaken;
    }

    public void setPhotoTaken(boolean photoTaken) {
        this.photoTaken = photoTaken;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
