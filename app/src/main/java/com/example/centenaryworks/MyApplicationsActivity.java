package com.example.centenaryworks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centenaryworks.adapter.JobStatusAdapter;
import com.example.centenaryworks.models.Job;
import com.example.centenaryworks.models.JobStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyApplicationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private JobStatusAdapter jobStatusAdapter;
    private List<JobStatus> jobStatusList;
    private DatabaseReference applicationsRef;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private TextView noApplicationsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_applications);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobStatusList = new ArrayList<>();
        noApplicationsTextView = findViewById(R.id.noApplicationsTextView);
        jobStatusAdapter = new JobStatusAdapter(jobStatusList, new JobStatusAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(JobStatus jobStatus) {
                // Handle item click, e.g., open a new activity with job details
                Intent intent = new Intent(MyApplicationsActivity.this, JobDetailsActivity.class);
                intent.putExtra("jobId", jobStatus.getJobId());
                intent.putExtra("FLAGS", "1");
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(jobStatusAdapter);

        applicationsRef = FirebaseDatabase.getInstance().getReference("Applications");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            loadUserApplications();
        }
    }

    private void loadUserApplications() {
        applicationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                jobStatusList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    JobStatus jobStatus = snapshot.getValue(JobStatus.class);
                    String key = snapshot.getKey();

                    if (jobStatus != null) {
                        checkApplicationStatus(key, jobStatus);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }


    private void checkApplicationStatus(String jobId, JobStatus jobStatus) {
        DatabaseReference userApplicationsRef = applicationsRef.child(jobId);

        userApplicationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User has an application for this job
                    DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("Jobs").child(jobId);

                    jobsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot jobSnapshot) {
                            if (jobSnapshot.exists()) {
                                String jobTitle = jobSnapshot.child("jobTitle").getValue(String.class);

                                if (dataSnapshot.child("AcceptedApplications").child(user.getUid()).exists()) {
                                    jobStatusList.add(new JobStatus(jobId, jobTitle, "Accepted"));
                                } else if (dataSnapshot.child("RejectedApplications").child(user.getUid()).exists()) {
                                    jobStatusList.add(new JobStatus(jobId, jobTitle, "Rejected"));
                                } else if (dataSnapshot.child(user.getUid()).exists()) {
                                    jobStatusList.add(new JobStatus(jobId, jobTitle, "Processing"));
                                }

                                jobStatusAdapter.notifyDataSetChanged();
                            }
                            else {
                                recyclerView.setVisibility(View.GONE);
                                noApplicationsTextView.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors if needed
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

}

