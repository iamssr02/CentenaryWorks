package com.example.centenaryworks.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centenaryworks.R;

import java.util.List;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.ViewHolder> {

    private List<String> workerNames;
    private OnWorkerClickListener listener;

    public WorkerAdapter(List<String> workerNames, OnWorkerClickListener listener) {
        this.workerNames = workerNames;
        this.listener = listener;
    }

    public interface OnWorkerClickListener {
        void onWorkerClick(int position);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView workerNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            workerNameTextView = itemView.findViewById(R.id.workerNameTextView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.worker_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String workerName = workerNames.get(position);
        holder.workerNameTextView.setText(workerName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onWorkerClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return workerNames.size();
    }

    public void updateWorkers(List<String> newWorkerNames) {
        workerNames.clear();
        workerNames.addAll(newWorkerNames);
        notifyDataSetChanged();
    }

}

