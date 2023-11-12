package com.example.centenaryworks;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WorkerDetailsActivity extends AppCompatActivity {

    private TextView nameTextView, ageTextView, genderTextView, experienceTextView;
    private Button acceptButton, rejectButton;
    private DatabaseReference workersRef, jobsRef, applicationsRef;
    private FirebaseAuth auth;
    private String workerId, jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_details);

        nameTextView = findViewById(R.id.workerNameTextView);
        ageTextView = findViewById(R.id.workerAgeTextView);
        genderTextView = findViewById(R.id.workerGenderTextView);
        experienceTextView = findViewById(R.id.workerExperienceTextView);
        acceptButton = findViewById(R.id.acceptButton);
        rejectButton = findViewById(R.id.rejectButton);

        auth = FirebaseAuth.getInstance();
        workersRef = FirebaseDatabase.getInstance().getReference("Workers");
        jobsRef = FirebaseDatabase.getInstance().getReference("Jobs");
        applicationsRef = FirebaseDatabase.getInstance().getReference("Applications");

        workerId = getIntent().getStringExtra("workerId");
        jobId = getIntent().getStringExtra("jobId");

        loadWorkerDetails();

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptApplication();
            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rejectApplication();
            }
        });
    }

    private void loadWorkerDetails() {
        workersRef.child(workerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Users worker = dataSnapshot.getValue(Users.class);
                    if (worker != null) {
                        nameTextView.setText("Name: " + worker.getName());
                        ageTextView.setText("Age: " + worker.getAge());
                        genderTextView.setText("Gender: " + worker.getGender());
                        experienceTextView.setText("Experience: " + worker.getWorkYears() + " years");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    private void acceptApplication() {
        // For example, decrease the number of openings for the job
        jobsRef.child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Job job = dataSnapshot.getValue(Job.class);
                    if (job != null) {
                        long currentOpenings = Long.parseLong(job.getNumberOfOpenings());
                        if (currentOpenings > 0) {
                            // Update the job with decreased openings
                            jobsRef.child(jobId).child("numberOfOpenings").setValue(String.valueOf(currentOpenings - 1));

                            // Add the worker's ID to the list of accepted applications
                            applicationsRef.child(jobId).child("AcceptedApplications").child(workerId).setValue(true);

                            // Remove the worker's application
                            applicationsRef.child(jobId).child(workerId).removeValue();
                            finish(); // Finish the activity after accepting the application
                        } else {
                            // Handle the case when there are no openings
                            Toast.makeText(WorkerDetailsActivity.this, "No openings available", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    private void rejectApplication() {
        // Remove the worker's application
        applicationsRef.child(jobId).child(workerId).removeValue();
        finish(); // Finish the activity after rejecting the application
    }
}
