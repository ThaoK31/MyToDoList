package com.example.mytodolist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytodolist.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {
    //
    private static final String ARG_CATEGORY_NAME = "category_name";
    private String categoryName; // Nom de la catégorie
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<String> tasks;

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
        // Initialiser RecyclerView et la liste des tâches
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(tasks);
        recyclerView.setAdapter(taskAdapter);

        // Ajouter une tâche fictive pour tester
        tasks.add("Exemple de tâche 1");
        tasks.add("Exemple de tâche 2");
        taskAdapter.notifyDataSetChanged();

        return view;
    }
}
