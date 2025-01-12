package com.example.mytodolist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
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
    private final OnTaskDeleteListener onTaskDeleteListener;
    private final OnTaskEditListener onTaskEditListener;
    private String currentUser;

    public interface OnTaskCheckedListener {
        void onTaskChecked(Task task);
    }

    public interface OnTaskDeleteListener {
        void onTaskDelete(Task task);
    }

    public interface OnTaskEditListener {
        void onTaskEdit(Task task);
    }

    public TaskAdapter(List<Task> taskList, List<Task> otherList, OnTaskCheckedListener onTaskCheckedListener, OnTaskDeleteListener onTaskDeleteListener, OnTaskEditListener onTaskEditListener) {
        this.taskList = taskList;
        this.otherList = otherList;
        this.onTaskCheckedListener = onTaskCheckedListener;
        this.onTaskDeleteListener = onTaskDeleteListener;
        this.onTaskEditListener = onTaskEditListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        // Récupérer l'utilisateur courant
        currentUser = parent.getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).
            getString("current_user", null);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskTitle.setText(task.getTitle());

        // Empêche les callbacks inutiles
        holder.taskCheckBox.setOnCheckedChangeListener(null);
        
        // Configure la checkbox et le style du texte
        holder.taskCheckBox.setChecked(task.isCompleted());
        if (task.isCompleted()) {
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskTitle.setAlpha(0.5f);
        } else {
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskTitle.setAlpha(1.0f);
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (onTaskDeleteListener != null) {
                onTaskDeleteListener.onTaskDelete(task);
            }
        });

        holder.taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            // Mettre à jour l'apparence immédiatement
            if (isChecked) {
                holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                holder.taskTitle.setAlpha(0.5f);
            } else {
                holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                holder.taskTitle.setAlpha(1.0f);
            }
            
            onTaskCheckedListener.onTaskChecked(task);

            // Vérifiez que l'ID et le nom de catégorie sont définis
            if (task.getId() == null || task.getCategoryName() == null || currentUser == null) {
                Log.e("TaskAdapter", "L'ID, le nom de catégorie ou l'utilisateur est null. Impossible de mettre à jour Firebase.");
                return;
            }

            // Mise à jour dans Firebase
            FirebaseUtils.getTaskRef(task, task.getCategoryName(), currentUser).child("completed").setValue(isChecked)
                .addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Log.d("TaskAdapter", "État de la tâche mis à jour dans Firebase.");
                    } else {
                        Log.e("TaskAdapter", "Erreur lors de la mise à jour de la tâche.", updateTask.getException());
                    }
                });
        });

        holder.editButton.setOnClickListener(v -> {
            if (onTaskEditListener != null) {
                onTaskEditListener.onTaskEdit(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;
        CheckBox taskCheckBox;
        ImageButton deleteButton;
        ImageButton editButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
            deleteButton = itemView.findViewById(R.id.deleteTaskButton);
            editButton = itemView.findViewById(R.id.editTaskButton);
        }
    }
}
