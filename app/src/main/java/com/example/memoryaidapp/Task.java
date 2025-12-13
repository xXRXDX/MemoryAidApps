package com.example.memoryaidapp;

public class Task {
    public long id;
    public String title;
    public String description;
    public String priority;
    public String timeText;
    public long scheduledMillis;

    // required empty constructor for some serializers (keeps safe)
    public Task() {}

    public Task(long id, String title, String description, String priority, String timeText, long scheduledMillis) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.timeText = timeText;
        this.scheduledMillis = scheduledMillis;
    }
}
