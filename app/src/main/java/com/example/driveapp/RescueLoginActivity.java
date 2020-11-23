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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;

public class RescueLoginActivity extends AppCompatActivity {

    private EditText registerEmail, registerPassword;
    private Button RegisterBtn, alreadyHaveAnAccount;

    private FirebaseAuth mAuth;
    private ProgressDialog loader;
    private  String currentUserOnlineID;
    private DatabaseReference userDatabaseRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_rescue_login);

        alreadyHaveAnAccount = findViewById(R.id.alreadyHaveAnAccount);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        RegisterBtn = findViewById(R.id.RegisterBtn);

        mAuth = FirebaseAuth.getInstance();

        loader = new ProgressDialog(this);

        alreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RescueLoginActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });


        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

    }

    private void performRegistration() {
        final String email = registerEmail.getText().toString();
        final  String password = registerPassword.getText().toString();



        if (TextUtils.isEmpty(email)){
            registerEmail.setError("Email Required!");
            return;
        }
        if (TextUtils.isEmpty(password)){
            registerPassword.setError("Password Required!");
            return;
        }
        else {
            loader.setMessage("Registration in progress...");
            loader.setCanceledOnTouchOutside(false);
            loader.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RescueLoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()){
                        String error = task.getException().toString();
                        Toast.makeText(RescueLoginActivity.this, "Registration Failed: \n" + error, Toast.LENGTH_SHORT).show();
                        loader.dismiss();
                    }else {
                        currentUserOnlineID = mAuth.getCurrentUser().getUid();
                        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child("rescue").child(currentUserOnlineID);

                        HashMap<String, Object> userInfo = new HashMap();
                        userInfo.put("id",currentUserOnlineID);
                        userInfo.put("email", email);
                        userInfo.put("type", "rescue");

                        userDatabaseRef.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(RescueLoginActivity.this, "Registration set successfully", Toast.LENGTH_SHORT).show();
                                }else {
                                    String error = task.getException().toString();
                                    Toast.makeText(RescueLoginActivity.this, "please provide details: "+ error, Toast.LENGTH_SHORT).show();
                                }
                                finish();
                                loader.dismiss();
                            }
                        });


                        Intent intent = new Intent(RescueLoginActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        loader.dismiss();
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}