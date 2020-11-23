package com.example.driveapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.google.firebase.database.FirebaseDatabase.*;

public class LoginActivity extends AppCompatActivity {
    private EditText registerEmail, registerPassword;
    private Button RegisterBtn;
    //private TextView dontHaveAccount;

    private FirebaseAuth mAuth;
    private ProgressDialog loader;
    private DatabaseReference userDatabaseRef;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        //dontHaveAccount = findViewById(R.id.dontHaveAccount);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        RegisterBtn = findViewById(R.id.RegisterBtn);
        mAuth = FirebaseAuth.getInstance();
        loader = new ProgressDialog(this);


        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String uid = user.getUid();
                    userDatabaseRef = getInstance().getReference("users").child(uid);
                    userDatabaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String type = snapshot.child("users").child("type").getValue(String.class);
                            if (type.equals("driver") || type.equals("rescue")) {
                                Intent intent = new Intent(LoginActivity.this, DriverMapActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Intent intent = new Intent(LoginActivity.this, RescueMapsActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        };

//


        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLogin();
            }
        });
    }

    private void performLogin() {
        final String email = registerEmail.getText().toString();
        final String password = registerPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            registerEmail.setError("Email Required!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            registerPassword.setError("Password Required!");
            return;
        } else {
            loader.setMessage("Login in progress...");
            loader.setCanceledOnTouchOutside(false);
            loader.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        String currentUserId = mAuth.getCurrentUser().getUid();
                        userDatabaseRef = getInstance().getReference("users").child(currentUserId);
                        userDatabaseRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String type = snapshot.child("type").getValue(String.class);
                                if (type.equals("driver")) {
                                    Intent intent = new Intent(LoginActivity.this, DriverMapActivity.class);
                                    startActivity(intent);
                                    finish();
                                    loader.dismiss();
                                } else if (type.equals("rescue")) {
                                    Intent intent = new Intent(LoginActivity.this, RescueMapsActivity.class);
                                    startActivity(intent);
                                    finish();
                                    loader.dismiss();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    } else {
                        String error = task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Login failed: \n" + error, Toast.LENGTH_SHORT).show();
                        loader.dismiss();
                    }

                }
            });
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }
}


