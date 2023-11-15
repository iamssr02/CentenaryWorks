package com.example.centenaryworks.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.centenaryworks.R;
import com.example.centenaryworks.models.Users;

import java.util.List;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.ViewHolder> {

    private List<Users> workerNames;
    private OnWorkerClickListener listener;

    public interface OnWorkerClickListener {
        void onWorkerClick(Users users);
    }

    public WorkerAdapter(List<Users> workerNames, OnWorkerClickListener listener) {
        this.workerNames = workerNames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.worker_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users users = workerNames.get(position);
        holder.bind(users, listener);
    }

    @Override
    public int getItemCount() {
        return workerNames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.workerNameTextView);
        }

        public void bind(final Users users, final OnWorkerClickListener listener) {
            nameTextView.setText(users.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onWorkerClick(users);
                }
            });
        }
    }
}