package com.example.centenaryworks;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centenaryworks.adapter.WorkerAdapter;
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

public class AcceptedApplicationsActivity extends AppCompatActivity {

    private FirebaseUser user;
    private RecyclerView workersRecyclerView;
    private WorkerAdapter workerAdapter;
    private List<Users> workerNames;
    private List<String> workerIds;
    private DatabaseReference workersRef, applicationsRef;
    private FirebaseAuth auth;
    private String workerId, jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_applications);

        auth = FirebaseAuth.getInstance();
        jobId = getIntent().getStringExtra("jobId");
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
                Intent intent = new Intent(AcceptedApplicationsActivity.this, WorkerDetailsActivity.class);
                intent.putExtra("workerId", users.getUid());
                intent.putExtra("jobId", jobId);
                intent.putExtra("FLAG",1);
                startActivity(intent);
            }
        });
        workersRecyclerView.setAdapter(workerAdapter);
        workersRef = FirebaseDatabase.getInstance().getReference("Workers");

        // Retrieve accepted worker IDs from the database
        applicationsRef.child(jobId).child("AcceptedApplications").addValueEventListener(new ValueEventListener() {
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

    private void loadWorkerNames() {
        // Retrieve worker names from the "Workers" node
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
}
