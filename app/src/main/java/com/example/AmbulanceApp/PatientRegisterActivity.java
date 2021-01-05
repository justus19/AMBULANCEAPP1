package com.example.AmbulanceApp;

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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class PatientRegisterActivity extends AppCompatActivity {

    private EditText registerEmail, registerPassword;
    private Button RegisterBtn, alreadyHaveAnAccount;


    private FirebaseAuth mAuth;
    private ProgressDialog loader;
    private DatabaseReference userDatabaseRef;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_patientlogin);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(PatientRegisterActivity.this, PatientMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        alreadyHaveAnAccount = findViewById(R.id.alreadyHaveAnAccount);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        RegisterBtn = findViewById(R.id.RegisterBtn);

        mAuth = FirebaseAuth.getInstance();


        loader = new ProgressDialog(this);

        alreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(PatientRegisterActivity.this, LoginActivity.class);
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
            loader.dismiss();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(PatientRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()){
                        String error = task.getException().toString();
                        Toast.makeText(PatientRegisterActivity.this, "Registration Failed: \n" + error, Toast.LENGTH_SHORT).show();
                        loader.dismiss();
                    }else {
                        String currentUserId = mAuth.getCurrentUser().getUid();
                        userDatabaseRef= FirebaseDatabase.getInstance().getReference().child("users").child("patient").child(currentUserId);


                        HashMap<String, Object> userInfo = new HashMap();
                        userInfo.put("id",currentUserId);
                        userInfo.put("email", email);
                        userInfo.put("type", "patient");

                        userDatabaseRef
                                .updateChildren(userInfo)
                                .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {

                                if (task.isSuccessful()){
                                    Toast.makeText(PatientRegisterActivity.this, "your have successfully registered", Toast.LENGTH_SHORT).show();

                                    loader.dismiss();
                                    Intent intent = new Intent(PatientRegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();


                                }else {

                                    loader.dismiss();
                                    String error = task.getException().toString();
                                    Toast.makeText(PatientRegisterActivity.this, "Details upload Failed: "+ error, Toast.LENGTH_SHORT).show();

                                }



                            }
                        });



                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}