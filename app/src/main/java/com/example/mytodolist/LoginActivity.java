package com.example.mytodolist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private EditText pseudoInput;
    private Button loginButton;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialiser la référence Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        
        // Initialiser les vues
        pseudoInput = findViewById(R.id.pseudoInput);
        loginButton = findViewById(R.id.loginButton);

        // Gérer le clic sur le bouton
        loginButton.setOnClickListener(v -> {
            String pseudo = pseudoInput.getText().toString().trim();
            if (pseudo.isEmpty()) {
                pseudoInput.setError("Le pseudo est requis");
                return;
            }
            checkAndLogin(pseudo);
        });
    }

    private void checkAndLogin(String pseudo) {
        usersRef.child(pseudo).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    // Le pseudo existe déjà, proposer de se connecter
                    new AlertDialog.Builder(this)
                        .setTitle("Compte existant")
                        .setMessage("Ce pseudo existe déjà. Voulez-vous vous connecter à ce compte ?")
                        .setPositiveButton("Se connecter", (dialog, which) -> {
                            // Se connecter au compte existant
                            connectUser(pseudo);
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
                } else {
                    // Créer un nouveau compte
                    createUser(pseudo);
                }
            } else {
                Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUser(String pseudo) {
        // Créer une référence pour les catégories de l'utilisateur
        DatabaseReference userCategoriesRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(pseudo)
            .child("categories");

        // Créer les catégories par défaut
        userCategoriesRef.child("Pro").setValue(true);
        userCategoriesRef.child("Perso").setValue(true);
        userCategoriesRef.child("Quotidiennes").setValue(true)
            .addOnCompleteListener(createTask -> {
                if (createTask.isSuccessful()) {
                    // Ajouter les tâches quotidiennes par défaut
                    DatabaseReference quotidiennesTasksRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(pseudo)
                        .child("categories")
                        .child("Quotidiennes")
                        .child("tasks");

                    // Créer les tâches par défaut
                    String task1Id = quotidiennesTasksRef.push().getKey();
                    String task2Id = quotidiennesTasksRef.push().getKey();
                    String task3Id = quotidiennesTasksRef.push().getKey();

                    Task task1 = new Task(task1Id, "Faire le lit", false, "Quotidiennes");
                    Task task2 = new Task(task2Id, "Se brosser les dents", false, "Quotidiennes");
                    Task task3 = new Task(task3Id, "Prendre une douche", false, "Quotidiennes");

                    quotidiennesTasksRef.child(task1Id).setValue(task1);
                    quotidiennesTasksRef.child(task2Id).setValue(task2);
                    quotidiennesTasksRef.child(task3Id).setValue(task3)
                        .addOnCompleteListener(tasksTask -> {
                            if (tasksTask.isSuccessful()) {
                                Toast.makeText(this, "Compte créé avec succès", Toast.LENGTH_SHORT).show();
                                connectUser(pseudo);
                            } else {
                                Toast.makeText(this, "Erreur lors de la création des tâches", Toast.LENGTH_SHORT).show();
                            }
                        });
                } else {
                    Toast.makeText(this, "Erreur lors de la création du compte", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void connectUser(String pseudo) {
        // Sauvegarder en local
        getSharedPreferences("user_prefs", MODE_PRIVATE)
            .edit()
            .putString("current_user", pseudo)
            .apply();
        
        // Rediriger vers MainActivity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
} 