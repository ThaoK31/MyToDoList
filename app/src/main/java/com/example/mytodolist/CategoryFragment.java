package com.example.mytodolist;

import android.app.AlertDialog;
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
        String categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        Log.d("CategoryFragment", "Nom de la catégorie : " + categoryName);

        // Initialiser la référence à la base de données
        //categories/
        //  categoryName/
        //    tasks/
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        taskRef = database.getReference("categories").child(categoryName).child("tasks");

        Log.d("CategoryFragment", "onCreateView: " + taskRef.toString());
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
            // Ajouter la tâche terminée dans la liste des tâches terminées
            completedTasks.add(task);
            tasks.remove(task);
            taskAdapter.notifyDataSetChanged();
            completedTaskAdapter.notifyDataSetChanged();
        });

        completedTaskAdapter = new TaskAdapter(completedTasks, tasks, task -> {
            // Ajouter la tâche de nouveau dans les tâches en cours
            tasks.add(task);
            completedTasks.remove(task);
            taskAdapter.notifyDataSetChanged();
            completedTaskAdapter.notifyDataSetChanged();
        });
        taskEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tasks.clear(); // Réinitialise la liste locale
                completedTasks.clear();
                if (snapshot.exists()) {
                    Log.d("CategoryFragment", "Nombre d'éléments dans snapshot : " + snapshot.getChildrenCount());
                    for (DataSnapshot taskSnapshot : snapshot.getChildren()) {

                        // Vérifiez si les données peuvent être converties en un objet Task
                        try {
                            Task task = taskSnapshot.getValue(Task.class); // Convertir les données Firebase en objet Task
                            if (task != null) {
                                if (task.isCompleted()) {
                                    completedTasks.add(task); // Ajouter aux tâches terminées
                                } else {
                                    tasks.add(task); // Ajouter aux tâches en cours
                                }
                            }
                        } catch (DatabaseException e) {
                            Log.e("CategoryFragment", "Erreur de conversion des données Firebase : " + e.getMessage());
                        }
                    }
                    taskAdapter.notifyDataSetChanged();
                    completedTaskAdapter.notifyDataSetChanged();
                }
                else {
                    Log.d("CategoryFragment", "Aucune tâche trouvée");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("APPX", "Failed to read value.", error.toException());
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
            Log.d("CategoryFragment", "Nom de la tâche : " + taskName);
            if (!taskName.isEmpty()) {
                String categoryName = getArguments().getString(ARG_CATEGORY_NAME);

            // Générer un ID unique pour la tâche
                DatabaseReference newTaskRef = taskRef.push();
                String id = newTaskRef.getKey();

                Log.d("CategoryFragment", "categoryName" + categoryName);
                Task newTask =new Task(id, taskName, false, categoryName);
                Log.d("CategoryFragment","Nouvelle tâche : " + newTask.getTitle());

                // Ajouter la tâche dans la base de données
                Log.d("CategoryFragment", taskRef.toString());
                newTaskRef.setValue(newTask).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("CategoryFragment", "Tâche ajoutée avec succès !");
                        taskAdapter.notifyDataSetChanged();

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
