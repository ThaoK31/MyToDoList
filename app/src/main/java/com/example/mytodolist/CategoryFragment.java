package com.example.mytodolist;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytodolist.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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


        recyclerViewTasks.setAdapter(taskAdapter);
        recyclerViewCompletedTasks.setAdapter(completedTaskAdapter);

        // Ajouter une tâche fictive pour tester
        tasks.add(new Task("Tâche en cours 1", false));
        tasks.add(new Task("Tâche en cours 2", false));
        completedTasks.add(new Task("Tâche terminée 1", true));


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
                tasks.add(new Task(taskName, false));
                taskAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
