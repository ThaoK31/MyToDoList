package com.example.mytodolist;

public class Task {
    private String title;
    private boolean isCompleted;
    private String categoryName;
    private String id;

    // Constructeur par défaut requis par Firebase
    public Task() {}
    public Task(String id, String title, boolean isCompleted, String categoryName) {
        this.id = id;
        this.title = title;
        this.isCompleted = isCompleted;
        this.categoryName = categoryName;
    }


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

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}