package com.example.mytodolist;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends FragmentStateAdapter {

    final List<String> categoryList; // Liste des noms de catégories

    public CategoryAdapter(@NonNull FragmentActivity fragmentActivity, List<String> initialCategories) {
        super(fragmentActivity);
        this.categoryList = new ArrayList<>(initialCategories);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position < categoryList.size()) {
            // Retourne un fragment pour la catégorie existante
            return CategoryFragment.newInstance(categoryList.get(position));
        } else {
            // Retourne un fragment vide
            return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        // +1 pour le fragment d'ajout de catégorie
        return categoryList.size();
    }

    public void addCategory(String categoryName) {
        categoryList.add(categoryName);
        notifyItemInserted(categoryList.size() - 1);
    }
}
