package com.example.mytodolist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytodolist.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {
    //
    private static final String ARG_CATEGORY_NAME = "category_name";
    private String categoryName;
    private String currentUser;

    private RecyclerView recyclerViewTasks;
    private RecyclerView recyclerViewCompletedTasks;

    private TaskAdapter taskAdapter;
    private TaskAdapter completedTaskAdapter;

    private List<Task> tasks;
    private List<Task> completedTasks;

    private DatabaseReference taskRef;

    private ValueEventListener taskEventListener;

    // Méthode de création d'une instance de CategoryFragment
    public static CategoryFragment newInstance(String categoryName) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        // Récupérer le nom de la catégorie
        categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        
        // Récupérer l'utilisateur courant
        currentUser = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("current_user", null);

        if (currentUser == null) {
            // Rediriger vers login si pas d'utilisateur
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
            return view;
        }

        // Initialiser la référence à la base de données
        taskRef = FirebaseUtils.getCategoryRef(categoryName, currentUser).child("tasks");

        // Initialisation des TextView pour les titres
        TextView categoryTitle = view.findViewById(R.id.textViewCategoryTitle);
        TextView completedTitle = view.findViewById(R.id.textViewCompletedTitle);

        // Mettre à jour les titres
        categoryTitle.setText(categoryName);
        completedTitle.setText("Terminé");


        // Initialiser RecyclerView et la liste des tâches
        recyclerViewTasks = view.findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerViewCompletedTasks = view.findViewById(R.id.recyclerViewCompletedTasks);
        recyclerViewCompletedTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialisation des listes
        tasks = new ArrayList<>();
        completedTasks = new ArrayList<>();

        // Initialisation des adaptateurs
        taskAdapter = new TaskAdapter(tasks, completedTasks, task -> {
            task.setCompleted(true);
            FirebaseUtils.getTaskRef(task, categoryName, currentUser).child("completed").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Log.d("CategoryFragment", "Tâche marquée comme terminée dans Firebase");
                })
                .addOnFailureListener(e -> {
                    Log.e("CategoryFragment", "Erreur lors de la mise à jour de l'état de la tâche", e);
                });
        }, task -> {
            FirebaseUtils.getTaskRef(task, categoryName, currentUser).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d("CategoryFragment", "Tâche supprimée de Firebase");
                })
                .addOnFailureListener(e -> {
                    Log.e("CategoryFragment", "Erreur lors de la suppression de la tâche", e);
                });
        });

        completedTaskAdapter = new TaskAdapter(completedTasks, tasks, task -> {
            task.setCompleted(false);
            FirebaseUtils.getTaskRef(task, categoryName, currentUser).child("completed").setValue(false)
                .addOnSuccessListener(aVoid -> {
                    Log.d("CategoryFragment", "Tâche marquée comme non terminée dans Firebase");
                })
                .addOnFailureListener(e -> {
                    Log.e("CategoryFragment", "Erreur lors de la mise à jour de l'état de la tâche", e);
                });
        }, task -> {
            FirebaseUtils.getTaskRef(task, categoryName, currentUser).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d("CategoryFragment", "Tâche supprimée de Firebase");
                })
                .addOnFailureListener(e -> {
                    Log.e("CategoryFragment", "Erreur lors de la suppression de la tâche", e);
                });
        });
        taskEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tasks.clear();
                completedTasks.clear();
                if (snapshot.exists()) {
                    Log.d("CategoryFragment", "Nombre de tâches trouvées : " + snapshot.getChildrenCount());
                    for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                        try {
                            Task task = taskSnapshot.getValue(Task.class); // Convertir les données Firebase en objet Task
                            if (task != null) {
                                if (task.isCompleted()) {
                                    completedTasks.add(task);
                                } else {
                                    tasks.add(task);
                                }
                            }
                        } catch (DatabaseException e) {
                            Log.e("CategoryFragment", "Erreur de conversion des données Firebase : " + e.getMessage());
                        }
                    }
                    taskAdapter.notifyDataSetChanged();
                    completedTaskAdapter.notifyDataSetChanged();
                } else {
                    Log.d("CategoryFragment", "Aucune tâche trouvée dans cette catégorie");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CategoryFragment", "Erreur de lecture des données Firebase", error.toException());
            }
        };
        taskRef.addValueEventListener(taskEventListener);


        recyclerViewTasks.setAdapter(taskAdapter);
        recyclerViewCompletedTasks.setAdapter(completedTaskAdapter);

        // Ajouter une tâche fictive pour tester
        //tasks.add(new Task("Tâche en cours 1", false));
        //tasks.add(new Task("Tâche en cours 2", false));
        //completedTasks.add(new Task("Tâche terminée 1", true));


        // Gestion du bouton d'ajout de tâche
        FloatingActionButton addTaskButton = view.findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(v -> showAddTaskDialog());

        return view;
    }
    // Méthode pour afficher une boîte de dialogue d'ajout de tâche
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Ajouter une tâche");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String taskName = input.getText().toString();
            if (!taskName.isEmpty()) {
                // Générer un ID unique pour la tâche
                DatabaseReference newTaskRef = taskRef.push();
                String id = newTaskRef.getKey();

                Task newTask = new Task(id, taskName, false, categoryName);
                
                FirebaseUtils.getTaskRef(newTask, categoryName, currentUser)
                    .setValue(newTask)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("CategoryFragment", "Tâche ajoutée avec succès !");
                        } else {
                            Log.e("CategoryFragment", "Erreur lors de l'ajout de la tâche", task.getException());
                        }
                    });
            }
        });
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
