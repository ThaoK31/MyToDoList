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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private CategoryAdapter categoryAdapter;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private DatabaseReference categoriesRef;



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
        ImageButton addButton = findViewById(R.id.addButton);

        // Catégories initiales
        List<String> initialCategories = Arrays.asList("Tâches", "Travail", "Personnel", "test1", "test2");
        categoryAdapter = new CategoryAdapter(this, initialCategories);

        // Associer l'adaptateur à la vue ViewPager
        viewPager.setAdapter(categoryAdapter);

        // Associer la vue TabLayout à la vue ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(categoryAdapter.categoryList.get(position));
            Log.d("TAG", "onCreate: ".concat(String.valueOf(position)));
        }).attach();
        addButton.setOnClickListener(v -> showAddCategoryDialog(categoryAdapter, tabLayout));


    }
    private void showAddCategoryDialog(CategoryAdapter categoryAdapter, TabLayout tabLayout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter une catégorie");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String categoryName = input.getText().toString();
            if (!categoryName.isEmpty()) {
                // Ajouter la catégorie à Firebase
                categoriesRef.child(categoryName).setValue(true).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("MainActivity", "Catégorie ajoutée avec succès." + categoryName);
                    } else {
                        Log.e("MainActivity", "Erreur lors de l'ajout de la catégorie.", task.getException());
                    }
                });
            }
        });
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadCategoriesFromFirebase() {
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Erreur de lecture des catégories.", error.toException());
            }
        });
    }
}

