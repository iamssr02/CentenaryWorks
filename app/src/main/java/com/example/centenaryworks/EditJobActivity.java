package com.example.centenaryworks;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class EditJobActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText openingsEditText;

    private TextView salaryTextView;
    private Button saveButton;
    private DatabaseReference jobsRef;
    private FirebaseAuth auth;
    private String jobId, title, description, numberOfOpenings, salary;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);

        auth = FirebaseAuth.getInstance();
        jobsRef = FirebaseDatabase.getInstance().getReference("Jobs");

        titleEditText = findViewById(R.id.editTitleEditText);
        descriptionEditText = findViewById(R.id.editDescriptionEditText);
        openingsEditText = findViewById(R.id.editOpeningsEditText);
        saveButton = findViewById(R.id.saveButton);
        salaryTextView = findViewById(R.id.salaryTextView);

        jobId = getIntent().getStringExtra("jobId");


        // Retrieve current job details and populate the EditText fields
        jobsRef.child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Job currentJob = dataSnapshot.getValue(Job.class);
                    if (currentJob != null) {
                        titleEditText.setText(currentJob.getJobTitle());
                        descriptionEditText.setText(currentJob.getJobDescription());
                        salaryTextView.setText("Salary: "+currentJob.getSalary());
                        openingsEditText.setText(String.valueOf(currentJob.getNumberOfOpenings()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges();
            }
        });
    }

    private void saveChanges() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String officialUid = user.getUid();
            String jobId = jobsRef.push().getKey();
            String newTitle = titleEditText.getText().toString().trim();
            String newDescription = descriptionEditText.getText().toString().trim();
            String openingsString = openingsEditText.getText().toString().trim();

            if (!newTitle.isEmpty() && !newDescription.isEmpty() && !openingsString.isEmpty()) {


                // Update the job details in the database
                Job updatedJob = new Job(jobId, newTitle, newDescription, officialUid, openingsString, salary);
                jobsRef.child(jobId).setValue(updatedJob);

                // Finish the activity
                finish();
            }
        }
    }
}