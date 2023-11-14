package com.example.centenaryworks.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centenaryworks.R;

import java.util.List;

public class AcceptedApplicationsAdapter extends RecyclerView.Adapter<AcceptedApplicationsAdapter.ViewHolder> {

    private List<String> acceptedApplications;
    private OnAcceptedApplicationClickListener clickListener;

    public AcceptedApplicationsAdapter(List<String> acceptedApplications, OnAcceptedApplicationClickListener clickListener) {
        this.acceptedApplications = acceptedApplications;
        this.clickListener = clickListener;
    }

    public void updateAcceptedApplications(List<String> acceptedApplications) {
        this.acceptedApplications = acceptedApplications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_accepted_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String workerName = acceptedApplications.get(position);
        holder.workerNameTextView.setText(workerName);
    }

    @Override
    public int getItemCount() {
        return acceptedApplications.size();
    }

    public interface OnAcceptedApplicationClickListener {
        void onAcceptedApplicationClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView workerNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            workerNameTextView = itemView.findViewById(R.id.workerNameTextView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                clickListener.onAcceptedApplicationClick(position);
            }
        }
    }
}
