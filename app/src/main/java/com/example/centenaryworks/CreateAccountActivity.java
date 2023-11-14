package com.example.centenaryworks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {

    ProgressBar mProgressBar;
    private GoogleSignInClient googleSignInClient;
    DatabaseReference database;
    FirebaseAuth auth;
    int flag = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mProgressBar = findViewById(R.id.phoneProgressBar);
        mProgressBar.setVisibility(View.GONE);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        CardView cardViewWorker = findViewById(R.id.gSignInWorkerBtn);
        CardView cardViewOfficial = findViewById(R.id.gSignInOfficalBtn);

        cardViewWorker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = 0;
                mProgressBar.setVisibility(View.VISIBLE);
                signInGoogle();
            }
        });
        cardViewOfficial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = 1;
                mProgressBar.setVisibility(View.VISIBLE);
                signInGoogle();
            }
        });

    }

    int RC_SIGN_IN = 40;

    private void signInGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken(), account);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void firebaseAuth(String idToken, GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    String currentUserId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

                    // Check if the user exists in Workers or Officials nodes
                    DatabaseReference workersRef = FirebaseDatabase.getInstance().getReference("Workers");
                    DatabaseReference officialsRef = FirebaseDatabase.getInstance().getReference("Officials");

                    workersRef.child(currentUserId).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot workersSnapshot) {
                            officialsRef.child(currentUserId).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                @Override
                                public void onSuccess(DataSnapshot officialsSnapshot) {
                                    if(flag == 0 && officialsSnapshot.exists()) {
                                        Toast.makeText(CreateAccountActivity.this,
                                                "Account already exists as official", Toast.LENGTH_LONG).show();
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                    else if (flag == 1 && workersSnapshot.exists()) {
                                        Toast.makeText(CreateAccountActivity.this,
                                                "Account already exists as worker", Toast.LENGTH_LONG).show();
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                    else if((flag == 1 && officialsSnapshot.exists()) || (flag == 0 && workersSnapshot.exists())){
                                        // User exists in either Workers or Officials node
                                        Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    }
                                    else {
                                        // User doesn't exist in either Workers or Officials node
                                        // Proceed to create a new account
                                        Intent intent = new Intent(CreateAccountActivity.this, DetailsActivity.class);
                                        intent.putExtra("EMAIL", user.getEmail());
                                        intent.putExtra("NAME", user.getDisplayName());
                                        intent.putExtra("FLAG", String.valueOf(flag));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(CreateAccountActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}