package com.example.mytodolist;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {
    public static DatabaseReference getTaskRef(Task task, String categoryName, String userId) {
        return FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("categories")
                .child(categoryName)
                .child("tasks")
                .child(task.getId());
    }

    public static DatabaseReference getCategoryRef(String categoryName, String userId) {
        return FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("categories")
                .child(categoryName);
    }
} 