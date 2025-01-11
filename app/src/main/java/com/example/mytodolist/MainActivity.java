package com.example.mytodolist;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private CategoryAdapter categoryAdapter;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private DatabaseReference categoriesRef;
    private MaterialButton addButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Gestion de l'appui sur le bouton retour
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Êtes-vous sûr de vouloir quitter?")
                        .setCancelable(false)
                        .setPositiveButton("Oui", (dialog, id) -> finish())
                        .setNegativeButton("Non", null)
                        .show();
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        categoriesRef = database.getReference("categories");

        // Charger les catégories depuis Firebase
        loadCategoriesFromFirebase();

        // Initialisation de l'adaptateur de catégories
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        addButton = findViewById(R.id.addButton);

        // Initialiser avec une liste vide
        categoryAdapter = new CategoryAdapter(this, new ArrayList<>());

        // Associer l'adaptateur à la vue ViewPager
        viewPager.setAdapter(categoryAdapter);
        
        // Empêcher le swipe horizontal si pas de catégories
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d("MainActivity", "Page sélectionnée: " + position);
            }
        });

        // Associer la vue TabLayout à la vue ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position < categoryAdapter.categoryList.size()) {
                tab.setText(categoryAdapter.categoryList.get(position));
            }
        }).attach();

        applyTabLongClickListeners();
        addButton.setOnClickListener(v -> showAddCategoryDialog(categoryAdapter, tabLayout));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Nettoyer les listeners pour éviter les fuites de mémoire
        if (viewPager != null) {
            viewPager.unregisterOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {});
        }
    }

    private void showAddCategoryDialog(CategoryAdapter categoryAdapter, TabLayout tabLayout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter une catégorie");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String categoryName = input.getText().toString().trim();
            if (categoryName.isEmpty()) {
                showError("Erreur", "Le nom de la catégorie ne peut pas être vide");
                return;
            }

            // Vérifier si la catégorie existe déjà
            categoriesRef.child(categoryName).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    showError("Erreur", "Cette catégorie existe déjà");
                    return;
                }

                // Ajouter la catégorie à Firebase
                categoriesRef.child(categoryName).setValue(true).addOnCompleteListener(addTask -> {
                    if (addTask.isSuccessful()) {
                        Log.d("MainActivity", "Catégorie ajoutée avec succès: " + categoryName);
                    } else {
                        showError("Erreur", "Impossible d'ajouter la catégorie");
                        Log.e("MainActivity", "Erreur lors de l'ajout de la catégorie", addTask.getException());
                    }
                });
            });
        });
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadCategoriesFromFirebase() {
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    int currentTab = viewPager.getCurrentItem(); // Sauvegarde l'onglet actuel

                    List<String> categories = new ArrayList<>();
                    for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                        categories.add(categorySnapshot.getKey()); // Le nom de la catégorie
                    }
                    categoryAdapter = new CategoryAdapter(MainActivity.this, categories);
                    viewPager.setAdapter(categoryAdapter);

                    // Associer la vue TabLayout à la vue ViewPager
                    new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                        tab.setText(categoryAdapter.categoryList.get(position));
                    }).attach();

                    // Réappliquer les listeners de clic long
                    applyTabLongClickListeners();
                    
                    // Restaurer l'onglet actif seulement s'il existe toujours
                    if (currentTab < categories.size()) {
                        viewPager.setCurrentItem(currentTab, false);
                    } else if (!categories.isEmpty()) {
                        viewPager.setCurrentItem(0, false);
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Erreur lors du chargement des catégories", e);
                    showError("Erreur", "Impossible de charger les catégories");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Erreur de lecture des catégories.", error.toException());
                showError("Erreur", "Impossible de charger les catégories");
            }
        });
    }

    private void showDeleteCategoryDialog(String categoryName) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer la catégorie")
                .setMessage("Voulez-vous vraiment supprimer la catégorie \"" + categoryName + "\" ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    // Supprimer la catégorie de Firebase
                    DatabaseReference categoryRef = categoriesRef.child(categoryName);
                    categoryRef.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("MainActivity", "Catégorie supprimée avec succès: " + categoryName);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("MainActivity", "Erreur lors de la suppression de la catégorie", e);
                            });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void applyTabLongClickListeners() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.view.setOnLongClickListener(v -> {
                    showDeleteCategoryDialog(tab.getText().toString());
                    return true;
                });
            }
        }
    }

    // Méthode utilitaire pour afficher les erreurs
    private void showError(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
