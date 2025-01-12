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
        usersRef.child(pseudo).setValue(true).addOnCompleteListener(createTask -> {
            if (createTask.isSuccessful()) {
                Toast.makeText(this, "Compte créé avec succès", Toast.LENGTH_SHORT).show();
                connectUser(pseudo);
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