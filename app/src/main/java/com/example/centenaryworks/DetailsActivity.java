package com.example.centenaryworks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.example.centenaryworks.models.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class DetailsActivity extends AppCompatActivity {

    String selectedGender, nameText, ageText, email, workYearText,flag;
    FirebaseAuth auth;
    DatabaseReference database;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Button btn = findViewById(R.id.login_button);

        Intent intent = getIntent();
        nameText = intent.getStringExtra("NAME");
        Log.d("tag", "onCreate: "+nameText);
        email = intent.getStringExtra("EMAIL");
        flag = intent.getStringExtra("FLAG");

        mProgressBar = findViewById(R.id.phoneProgressBar);
        mProgressBar.setVisibility(View.GONE);

        EditText name, age, workYears;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        name = findViewById(R.id.nameText);
        name.setText(nameText);
        age = findViewById(R.id.ageText);
        workYears = findViewById(R.id.workYearText);

        Spinner genderSpinner = findViewById(R.id.genderSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedGender = parentView.getItemAtPosition(position).toString();
                }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case where nothing is selected if needed
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameText = name.getText().toString();
                ageText = age.getText().toString();
                workYearText = workYears.getText().toString();
                mProgressBar.setVisibility(View.VISIBLE);
                if (nameText!=null && ageText!=null){
                    FirebaseUser user = auth.getCurrentUser();

                    Users users = new Users(nameText, email, user.getUid(), ageText, selectedGender, workYearText);

                    if(Objects.equals(flag, "0")){
                        database.child("Workers").child(user.getUid()).setValue(users);
                        Intent intent = new Intent(DetailsActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else{
                        database.child("Officials").child(user.getUid()).setValue(users);
                        Intent intent = new Intent(DetailsActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        });
    }
}