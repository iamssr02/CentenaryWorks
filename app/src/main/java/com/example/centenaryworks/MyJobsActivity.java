package com.example.centenaryworks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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

public class MyJobsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;
    private List<Job> jobList;
    private DatabaseReference jobsRef;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private TextView noJobsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_jobs);

        auth = FirebaseAuth.getInstance();
        jobsRef = FirebaseDatabase.getInstance().getReference("Jobs");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobList = new ArrayList<>();
        noJobsTextView = findViewById(R.id.noJobsTextView);
        jobAdapter = new JobAdapter(jobList, new JobAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Job job) {
                Intent intent = new Intent(MyJobsActivity.this, JobDetailsActivity.class);
                intent.putExtra("jobId", job.getJobId());
                intent.putExtra("FLAGS", 2);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(jobAdapter);

        user = auth.getCurrentUser();
        if (user != null) {
            loadOfficialJobs(user.getUid());
        }
    }

    private void loadOfficialJobs(String officialUid) {
        jobsRef.orderByChild("officialUid").equalTo(officialUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                jobList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Job job = snapshot.getValue(Job.class);

                    // Check if the job has zero openings
                    if (job != null && Integer.parseInt(job.getNumberOfOpenings()) == 0) {
                        jobList.add(job);
                    }
                }
                jobAdapter.notifyDataSetChanged();
                if (jobList.isEmpty()){
                    noJobsTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }
}
