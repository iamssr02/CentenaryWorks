package com.example.centenaryworks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.centenaryworks.adapter.JobAdapter;
import com.example.centenaryworks.adapter.WorkerAdapter;
import com.example.centenaryworks.models.Job;
import com.example.centenaryworks.models.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class JobDetailsActivity extends AppCompatActivity {

    private TextView jobTitleTextView, jobDescriptionTextView, appliedCandidatesTextView, numberofOpeningsTextView, salaryTextView, dateTextView;
    private Button applyButton, unapplyButton, editJobButton, deleteJobButton, acceptedApplicationsButton;
    private String jobId;
    private int flags;
    private DatabaseReference jobsRef, applicationsRef;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private RecyclerView workersRecyclerView;
    private WorkerAdapter workerAdapter;
    private List<Users> workerNames;
    private List<String> workerIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);

        jobTitleTextView = findViewById(R.id.jobTitleTextView);
        jobDescriptionTextView = findViewById(R.id.jobDescriptionTextView);
        appliedCandidatesTextView = findViewById(R.id.appliedCandidatesTextView);
        numberofOpeningsTextView = findViewById(R.id.numberOfOpeningsTextView);
        salaryTextView = findViewById(R.id.salaryTextView);
        dateTextView = findViewById(R.id.dateTextView);

        applyButton = findViewById(R.id.applyButton);
        unapplyButton = findViewById(R.id.unapplyButton);
        editJobButton = findViewById(R.id.editJobButton);
        deleteJobButton = findViewById(R.id.deleteJobButton);
        acceptedApplicationsButton = findViewById(R.id.acceptedApplicationsButton);

        auth = FirebaseAuth.getInstance();
        jobsRef = FirebaseDatabase.getInstance().getReference("Jobs");
        jobId = getIntent().getStringExtra("jobId");

        if(getIntent().hasExtra("FLAGS")){
            flags = 1;
        }

        applicationsRef = FirebaseDatabase.getInstance().getReference("Applications");
        user = auth.getCurrentUser();

        workersRecyclerView = findViewById(R.id.recyclerView);
        workersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        workerNames = new ArrayList<>();
        workerIds = new ArrayList<>();
        workerAdapter = new WorkerAdapter(workerNames, new WorkerAdapter.OnWorkerClickListener() {
            @Override
            public void onWorkerClick(Users users) {
                // Handle click on a worker item in the RecyclerView
                Intent intent = new Intent(JobDetailsActivity.this, WorkerDetailsActivity.class);
                intent.putExtra("workerId", users.getUid());
                intent.putExtra("jobId", jobId);
                startActivity(intent);
            }
        });
        workersRecyclerView.setAdapter(workerAdapter);

        loadJobDetails();
        checkApplicationStatus();
        checkUserStatus();

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyForJob();
            }
        });
        unapplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unapplyForJob();
            }
        });
        editJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editJob();
            }
        });
        deleteJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteJob();
            }
        });
        acceptedApplicationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewAcceptedApplications();
            }
        });
    }

    private void viewAcceptedApplications() {
        Intent intent = new Intent(JobDetailsActivity.this, AcceptedApplicationsActivity.class);
        intent.putExtra("jobId", jobId);
        startActivity(intent);
    }

    private void checkUserStatus() {
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Workers").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User is a worker
                        if(flags==1){
                            applyButton.setVisibility(View.GONE);
                            unapplyButton.setVisibility(View.GONE);
                        }
                        deleteJobButton.setVisibility(View.GONE);
                        editJobButton.setVisibility(View.GONE);
                        acceptedApplicationsButton.setVisibility(View.GONE);
                    } else {
                        // User is an official
                        applyButton.setVisibility(View.GONE);
                        unapplyButton.setVisibility(View.GONE);
                        workersRecyclerView.setVisibility(View.VISIBLE);
                        applicationsRef.child(jobId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot workerSnapshot : dataSnapshot.getChildren()) {
                                        String workerId = workerSnapshot.getKey();
                                        if (workerId != null) {
                                            workerIds.add(workerId);
                                        }
                                    }
                                    // Load worker names into RecyclerView
                                    loadWorkerNames();
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

    private void loadWorkerNames() {
        // Retrieve worker names from the "Workers" node
        DatabaseReference workersRef = FirebaseDatabase.getInstance().getReference("Workers");
        workersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (String workerId : workerIds) {
                    DataSnapshot workerSnapshot = dataSnapshot.child(workerId);
                    if (workerSnapshot.exists()) {
                        Users worker = workerSnapshot.getValue(Users.class);
                        if (worker != null) {
                            workerNames.add(worker);
                        }
                    }
                }
                workerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    private void checkApplicationStatus() {
        if (user != null) {
            String userId = user.getUid();
            applicationsRef.child(jobId).child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            boolean hasApplied = dataSnapshot.exists();
                            updateUI(hasApplied);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors if needed
                        }
                    });
        }
    }

    private void loadJobDetails() {
        jobsRef.child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Job job = dataSnapshot.getValue(Job.class);
                    jobTitleTextView.setText(job.getJobTitle());
                    jobDescriptionTextView.setText(job.getJobDescription());
                    numberofOpeningsTextView.setText("Number of Openings: " + job.getNumberOfOpenings());
                    salaryTextView.setText("Salary: " + job.getSalary());
                    dateTextView.setText("Posted Date: " + job.getDate());
                    loadAppliedCandidates();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    private void loadAppliedCandidates() {
        applicationsRef.child(jobId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                appliedCandidatesTextView.setText("Applied Candidates: " + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    private void applyForJob() {

        if (user != null) {
            String workerUid = user.getUid();
            applicationsRef.child(jobId).child(workerUid).setValue(true);
            Toast.makeText(JobDetailsActivity.this, "Applied for the job", Toast.LENGTH_SHORT).show();
            updateUI(true);
        }
    }

    private void unapplyForJob() {
        if (user != null) {
            String userId = user.getUid();
            applicationsRef.child(jobId).child(userId).removeValue();
            updateUI(false);
        }
    }

    private void updateUI(boolean hasApplied) {
        if(flags==1){
            applyButton.setVisibility(View.GONE);
            unapplyButton.setVisibility(View.GONE);
        }
        else {
            applyButton.setVisibility(hasApplied ? View.GONE : View.VISIBLE);
            unapplyButton.setVisibility(hasApplied ? View.VISIBLE : View.GONE);
        }
    }

    private void editJob() {
        Intent intent = new Intent(JobDetailsActivity.this, EditJobActivity.class);
        intent.putExtra("jobId", jobId);
        startActivity(intent);
    }

    private void deleteJob() {
        // Delete the job and navigate back to the previous screen
        jobsRef.child(jobId).removeValue();
        applicationsRef.child(jobId).removeValue();
        finish();
    }
}
