package com.example.mytodolist;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class MyToDoListApp extends Application {
    private static MyToDoListApp instance;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Initialisation des préférences
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        
        // Appliquer le thème au démarrage
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
            isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public static MyToDoListApp getInstance() {
        return instance;
    }

    public boolean isUserLoggedIn() {
        return prefs.getString("current_user", null) != null;
    }
} 