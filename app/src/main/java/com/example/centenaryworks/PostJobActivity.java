package com.example.centenaryworks;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.centenaryworks.models.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostJobActivity extends AppCompatActivity {

    private EditText jobTitleEditText;
    private EditText jobDescriptionEditText;
    private EditText numberOfOpeningsEditText;
    private EditText salaryEditText;
    private Button postJobButton;
    private DatabaseReference jobsRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);

        auth = FirebaseAuth.getInstance();
        jobsRef = FirebaseDatabase.getInstance().getReference("Jobs");

        jobTitleEditText = findViewById(R.id.jobTitleEditText);
        jobDescriptionEditText = findViewById(R.id.jobDescriptionEditText);
        numberOfOpeningsEditText = findViewById(R.id.numberOfOpeningsEditText);
        salaryEditText = findViewById(R.id.salaryEditText);
        postJobButton = findViewById(R.id.postJobButton);

        postJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postJob();
            }
        });
    }

    private void postJob() {
        String jobTitle = jobTitleEditText.getText().toString().trim();
        String jobDescription = jobDescriptionEditText.getText().toString().trim();
        String numberOfOpenings = numberOfOpeningsEditText.getText().toString().trim();
        String salary = salaryEditText.getText().toString().trim();

        // Get today's date
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(today);

        if (!jobTitle.isEmpty() && !jobDescription.isEmpty() && !numberOfOpenings.isEmpty() && !salary.isEmpty()) {


            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                String officialUid = user.getUid();
                String jobId = jobsRef.push().getKey();

                Job job = new Job(jobId, jobTitle, jobDescription, officialUid, numberOfOpenings, salary, formattedDate);
                jobsRef.child(jobId).setValue(job);

                finish(); // Close the activity after posting
            }
        }
    }
}
