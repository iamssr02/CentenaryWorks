package com.example.centenaryworks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JobDetailsActivity extends AppCompatActivity {

    private TextView jobTitleTextView, jobDescriptionTextView, appliedCandidatesTextView, numberofOpeningsTextView, salaryTextView;
    private Button applyButton, unapplyButton, editJobButton, deleteJobButton;
    private String jobId;
    private DatabaseReference jobsRef, applicationsRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);

        jobTitleTextView = findViewById(R.id.jobTitleTextView);
        jobDescriptionTextView = findViewById(R.id.jobDescriptionTextView);
        appliedCandidatesTextView = findViewById(R.id.appliedCandidatesTextView);
        numberofOpeningsTextView = findViewById(R.id.numberOfOpeningsTextView);
        salaryTextView = findViewById(R.id.salaryTextView);
        applyButton = findViewById(R.id.applyButton);
        unapplyButton = findViewById(R.id.unapplyButton);
        editJobButton = findViewById(R.id.editJobButton);
        deleteJobButton = findViewById(R.id.deleteJobButton);

        auth = FirebaseAuth.getInstance();
        jobsRef = FirebaseDatabase.getInstance().getReference("Jobs");
        applicationsRef = FirebaseDatabase.getInstance().getReference("Applications");

        jobId = getIntent().getStringExtra("jobId");

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
    }

    private void checkUserStatus() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Workers").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User is a worker
                        deleteJobButton.setVisibility(View.GONE);
                        editJobButton.setVisibility(View.GONE);
                    } else {
                        // User is an official
                        applyButton.setVisibility(View.GONE);
                        unapplyButton.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors if needed
                }
            });
        }
    }

    private void checkApplicationStatus() {
        FirebaseUser user = auth.getCurrentUser();
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
                    numberofOpeningsTextView.setText("Number of Openings: "+job.getNumberOfOpenings());
                    salaryTextView.setText("Salary: "+job.getSalary());
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
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String workerUid = user.getUid();
            applicationsRef.child(jobId).child(workerUid).setValue(true);
            Toast.makeText(JobDetailsActivity.this, "Applied for the job", Toast.LENGTH_SHORT).show();
            updateUI(true);
        }
    }

    private void unapplyForJob() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            applicationsRef.child(jobId).child(userId).removeValue();
            updateUI(false);
        }
    }
    private void updateUI(boolean hasApplied) {
        applyButton.setVisibility(hasApplied ? View.GONE : View.VISIBLE);
        unapplyButton.setVisibility(hasApplied ? View.VISIBLE : View.GONE);
    }

    private void editJob() {
        Intent intent = new Intent(JobDetailsActivity.this, EditJobActivity.class);
        intent.putExtra("jobId", jobId);
        startActivity(intent);
    }

    private void deleteJob() {
        // Delete the job and navigate back to the previous screen
        jobsRef.child(jobId).removeValue();
        finish();
    }
}
