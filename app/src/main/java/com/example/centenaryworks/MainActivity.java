package com.example.centenaryworks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.centenaryworks.adapter.JobAdapter;
import com.example.centenaryworks.models.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;
    private List<Job> jobList;
    private DatabaseReference jobsRef;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        jobsRef = FirebaseDatabase.getInstance().getReference("Jobs");

        Button postJobButton = findViewById(R.id.postJobButton);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList, new JobAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Job job) {
                // Handle item click, e.g., open a new activity with job details
                Intent intent = new Intent(MainActivity.this, JobDetailsActivity.class);
                intent.putExtra("jobId", job.getJobId());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(jobAdapter);

        user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Workers").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User is a worker
                        loadWorkerJobs();
                    } else {
                        // User is an official
                        postJobButton.setVisibility(View.VISIBLE);
                        loadOfficialJobs(uid);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors if needed
                }
            });
        }

        postJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the click to post a new job
                Intent intent = new Intent(MainActivity.this, PostJobActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadWorkerJobs() {
        jobsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                jobList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("TAG", "onDataChange: " + snapshot.toString());
                    Job job = snapshot.getValue(Job.class);

                    DatabaseReference acceptedApplicationsRef = FirebaseDatabase.getInstance().getReference("Applications")
                            .child(job.getJobId()).child("AcceptedApplications");
                    DatabaseReference rejectedApplicationsRef = FirebaseDatabase.getInstance().getReference("Applications")
                            .child(job.getJobId()).child("RejectedApplications");

                    checkApplicationStatus(user.getUid(), acceptedApplicationsRef, job, new Callback() {
                        @Override
                        public void onCallback(int result) {
                            if (result == 1) {
                                checkApplicationStatus(user.getUid(), rejectedApplicationsRef, job, new Callback() {
                                    @Override
                                    public void onCallback(int result) {
                                        if (result == 1) {
                                            jobList.add(job);
                                            jobAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                            }
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

    private void checkApplicationStatus(String workerId, DatabaseReference applicationsRef, Job job, Callback callback) {
        applicationsRef.child(workerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int flag = 0;
                if (!dataSnapshot.exists()) {
                    flag = 1;
                }
                callback.onCallback(flag);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    interface Callback {
        void onCallback(int result);
    }


    private void loadOfficialJobs(String officialUid) {
        jobsRef.orderByChild("officialUid").equalTo(officialUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                jobList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Job job = snapshot.getValue(Job.class);
                    jobList.add(job);
                }
                jobAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }
}