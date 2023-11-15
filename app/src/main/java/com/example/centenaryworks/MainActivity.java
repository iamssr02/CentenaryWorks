package com.example.centenaryworks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.centenaryworks.adapter.JobAdapter;
import com.example.centenaryworks.models.Job;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
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
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        jobsRef = FirebaseDatabase.getInstance().getReference("Jobs");

        Button postJobButton = findViewById(R.id.postJobButton);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

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
                        setupWorkerNavigation();
                    } else {
                        // User is an official
                        postJobButton.setVisibility(View.VISIBLE);
                        loadOfficialJobs(uid);
                        setupOfficialNavigation();
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

        // Set up the Toolbar and ActionBarDrawerToggle
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void loadWorkerJobs() {
        jobsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                jobList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Job job = snapshot.getValue(Job.class);

                    // Check if numberOfOpenings is greater than 0
                    if (job != null && Integer.parseInt(job.getNumberOfOpenings()) > 0) {

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

                    // Check if numberOfOpenings is greater than 0
                    if (job != null && Integer.parseInt(job.getNumberOfOpenings()) > 0) {
                        jobList.add(job);
                    }
                }
                jobAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }


    private void setupWorkerNavigation() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.drawer_menu_worker);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_my_applications) {
                    startActivity(new Intent(MainActivity.this, MyApplicationsActivity.class));
                } else if (itemId == R.id.nav_logout) {
                    mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
                drawerLayout.closeDrawers(); // Close the drawer after selecting an item
                return true;
            }
        });
    }

    private void setupOfficialNavigation() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.drawer_menu_official);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_my_jobs) {
                    startActivity(new Intent(MainActivity.this, MyJobsActivity.class));
                } else if (itemId == R.id.nav_logout) {
                    mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
                drawerLayout.closeDrawers(); // Close the drawer after selecting an item
                return true;
            }
        });
    }
}
