package com.example.mytodolist;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private List<Task> otherList; // Liste pour déplacer la tâche
    private final OnTaskCheckedListener onTaskCheckedListener;

    public interface OnTaskCheckedListener {
        void onTaskChecked(Task task);
    }
    public TaskAdapter(List<Task> taskList, List<Task> otherList, OnTaskCheckedListener onTaskCheckedListener) {
        this.taskList = taskList;
        this.otherList = otherList;
        this.onTaskCheckedListener = onTaskCheckedListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskTitle.setText(task.getTitle());

        // Empêche les callbacks inutiles
        holder.taskCheckBox.setOnCheckedChangeListener(null);
        // Configure la checkbox
        holder.taskCheckBox.setChecked(task.isCompleted());

        holder.taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked); // Mettre à jour l'état de la tâche
            onTaskCheckedListener.onTaskChecked(task);
            notifyItemChanged(holder.getAdapterPosition());

            // Vérifiez que l'ID et le nom de catégorie sont définis
            if (task.getId() == null || task.getCategoryName() == null) {
                Log.e("TaskAdapter", "L'ID ou le nom de catégorie est null. Impossible de mettre à jour Firebase.");
                return;
            }
            else {
                Log.d("TaskAdapter", "ID de la tâche: " + task.getId());
                Log.d("TaskAdapter", "Nom de la catégorie: " + task.getCategoryName());
            }

            // Référence Firebase pour la tâche
            DatabaseReference taskRef = FirebaseDatabase.getInstance()
                    .getReference("categories")
                    .child(task.getCategoryName()) // Assurez-vous que la tâche a une propriété `categoryName` si nécessaire
                    .child("tasks")
                    .child(task.getId());

            // Mise à jour partielle des champs
                taskRef.child("completed").setValue(isChecked).addOnCompleteListener(updatetask -> {
                    if (updatetask.isSuccessful()) {
                        Log.d("TaskAdapter", "État de la tâche mis à jour dans Firebase.");
                    } else {
                        Log.e("TaskAdapter", "Erreur lors de la mise à jour de la tâche.", updatetask.getException());
                    }
                });

            notifyItemChanged(holder.getAdapterPosition());

        });

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;
        CheckBox taskCheckBox;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
        }
    }
}
