package com.example.mytodolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private String currentUser;
    private SharedPreferences prefs;
    private SwitchMaterial switchDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialisation des préférences
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUser = prefs.getString("current_user", null);

        // Initialisation des vues
        TextView textViewCurrentUser = findViewById(R.id.textViewCurrentUser);
        MaterialButton buttonLogout = findViewById(R.id.logoutButton);
        MaterialButton buttonDeleteAccount = findViewById(R.id.deleteAccountButton);
        MaterialButton buttonToMain = findViewById(R.id.homeButton);
        MaterialButton buttonToAbout = findViewById(R.id.aboutButton);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        // Afficher l'utilisateur actuel
        textViewCurrentUser.setText("Utilisateur actuel : " + currentUser);

        // Configuration des listeners
        buttonLogout.setOnClickListener(this);
        buttonDeleteAccount.setOnClickListener(this);
        buttonToMain.setOnClickListener(this);
        buttonToAbout.setOnClickListener(this);

        // Configuration du switch mode sombre
        boolean isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        switchDarkMode.setChecked(isDarkMode);
        switchDarkMode.setOnCheckedChangeListener((view, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.logoutButton) {
            alertDialogLogout();
        } else if (viewId == R.id.deleteAccountButton) {
            alertDialogDeleteAccount();
        } else if (viewId == R.id.homeButton) {
            goToMain();
        } else if (viewId == R.id.aboutButton) {
            goToAbout();
        }
    }

    private void alertDialogLogout() {
        new AlertDialog.Builder(this)
            .setTitle("Déconnexion")
            .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
            .setPositiveButton("Oui", (dialog, which) -> {
                prefs.edit().remove("current_user").apply();
                goToLogin();
            })
            .setNegativeButton("Non", null)
            .show();
    }

    private void alertDialogDeleteAccount() {
        new AlertDialog.Builder(this)
            .setTitle("Supprimer le compte")
            .setMessage("Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.")
            .setPositiveButton("Supprimer", (dialog, which) -> deleteUserAccount())
            .setNegativeButton("Annuler", null)
            .show();
    }

    private void deleteUserAccount() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(currentUser);
        
        userRef.removeValue()
            .addOnSuccessListener(d -> {
                Log.d("SettingsActivity", "Compte utilisateur supprimé avec succès");
                prefs.edit().clear().apply();
                goToLogin();
            })
            .addOnFailureListener(e -> {
                Log.e("SettingsActivity", "Erreur lors de la suppression du compte", e);
                showErrorDialog();
            });
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Erreur")
            .setMessage("Une erreur est survenue lors de la suppression du compte.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void goToAbout() {
        startActivity(new Intent(this, AboutActivity.class));
        finish();
    }
}