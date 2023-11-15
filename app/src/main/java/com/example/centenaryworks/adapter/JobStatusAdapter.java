package com.example.centenaryworks.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centenaryworks.R;
import com.example.centenaryworks.models.Job;
import com.example.centenaryworks.models.JobStatus;

import java.util.List;

public class JobStatusAdapter extends RecyclerView.Adapter<JobStatusAdapter.ViewHolder> {

    private List<JobStatus> jobList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(JobStatus jobStatus);
    }

    public JobStatusAdapter(List<JobStatus> jobList, OnItemClickListener listener) {
        this.jobList = jobList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JobStatus jobStatus = jobList.get(position);
        holder.bind(jobStatus, listener);
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView titleTextView;
        private TextView descriptionTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }

        public void bind(final JobStatus jobStatus, final OnItemClickListener listener) {
            titleTextView.setText(jobStatus.getJobTitle());
            descriptionTextView.setText(jobStatus.getJobDescription());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(jobStatus);
                }
            });
        }
    }
}

