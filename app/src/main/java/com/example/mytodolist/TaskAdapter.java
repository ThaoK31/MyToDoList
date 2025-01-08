package com.example.mytodolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<String> taskList;
    private List<String> otherList; // Liste pour déplacer la tâche
    private OnTaskCheckedListener onTaskCheckedListener;

    public interface OnTaskCheckedListener {
        void onTaskChecked(String task);
    }
    public TaskAdapter(List<String> taskList, List<String> otherList, OnTaskCheckedListener onTaskCheckedListener) {
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
        String task = taskList.get(position);
        holder.taskTitle.setText(task);

        // Configure la checkbox
        holder.taskCheckBox.setOnCheckedChangeListener(null); // Empêche les callbacks inutiles
        holder.taskCheckBox.setChecked(otherList.contains(task));

        holder.taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                onTaskCheckedListener.onTaskChecked(task); // Déplacer la tâche
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

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
        }
    }
}
