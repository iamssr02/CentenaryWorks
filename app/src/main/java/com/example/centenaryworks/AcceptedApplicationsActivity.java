package com.example.centenaryworks;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AcceptedApplicationsActivity extends AppCompatActivity implements AcceptedApplicationsAdapter.OnAcceptedApplicationClickListener {

    private RecyclerView acceptedApplicationsRecyclerView;

    private String jobId;
    private AcceptedApplicationsAdapter acceptedApplicationsAdapter;
    private List<String> acceptedWorkerIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_applications);

        acceptedApplicationsRecyclerView = findViewById(R.id.acceptedApplicationsRecyclerView);
        acceptedApplicationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        acceptedApplicationsAdapter = new AcceptedApplicationsAdapter(new ArrayList<>(), this);
        acceptedApplicationsRecyclerView.setAdapter(acceptedApplicationsAdapter);
        jobId = getIntent().getStringExtra("jobId");

        // Retrieve accepted worker IDs from the database
        loadAcceptedWorkerIds();
    }

    private void loadAcceptedWorkerIds() {
        DatabaseReference acceptedApplicationsRef = FirebaseDatabase.getInstance()
                .getReference("Applications").child("AcceptedApplications");

        acceptedApplicationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    acceptedWorkerIds = new ArrayList<>();
                    for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot workerSnapshot : jobSnapshot.getChildren()) {
                            String workerId = workerSnapshot.getKey();
                            if (workerId != null && !acceptedWorkerIds.contains(workerId)) {
                                acceptedWorkerIds.add(workerId);
                            }
                        }
                    }
                    // Load accepted worker names into RecyclerView
                    loadAcceptedWorkerNames();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    private void loadAcceptedWorkerNames() {
        DatabaseReference workersRef = FirebaseDatabase.getInstance().getReference("Workers");
        workersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> acceptedWorkerNames = new ArrayList<>();
                for (String workerId : acceptedWorkerIds) {
                    DataSnapshot workerSnapshot = dataSnapshot.child(workerId);
                    if (workerSnapshot.exists()) {
                        Users worker = workerSnapshot.getValue(Users.class);
                        if (worker != null) {
                            acceptedWorkerNames.add(worker.getName());
                        }
                    }
                }
                // Update RecyclerView with accepted worker names
                acceptedApplicationsAdapter.updateAcceptedApplications(acceptedWorkerNames);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    @Override
    public void onAcceptedApplicationClick(int position) {
        // Handle item click as needed
    }
}
