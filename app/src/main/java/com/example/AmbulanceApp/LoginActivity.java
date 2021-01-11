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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText registerEmail, registerPassword;
    private Button RegisterBtn;
    private TextView dontHaveAccount;

    private FirebaseAuth mAuth;
    private ProgressDialog loader;
    private DatabaseReference userDatabaseRef;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        dontHaveAccount = findViewById(R.id.noAccTV);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        RegisterBtn = findViewById(R.id.RegisterBtn);
        mAuth = FirebaseAuth.getInstance();
        loader = new ProgressDialog(this);

        dontHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), DriverRegisterActivity.class);
                startActivity(intent);
            }
        });

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


                    if (task.isSuccessful()){
                        String currentUserId = mAuth.getCurrentUser().getUid();
                        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
                        userDatabaseRef.addValueEventListener(new ValueEventListener() {

                            String type;
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                                if (snapshot.exists()) {

                                    if (map.get("type") != null) {

                                        type = map.get("type").toString();

                                    }


                                    if (type.equals("patient")){
                                        Intent intent = new Intent(LoginActivity.this, PatientMapActivity.class);
                                        startActivity(intent);
                                        finish();
                                        loader.dismiss();

                                    }else if (type.equals("driver")){

                                        Intent intent = new Intent(LoginActivity.this, DriverMapsActivity.class);
                                        startActivity(intent);
                                        finish();
                                        loader.dismiss();

                                    }

                                }else{

                                    loader.dismiss();
                                    Toast.makeText(LoginActivity.this, "Account details successfull", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }else {

                        String error = task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Login failed: \n" + error, Toast.LENGTH_SHORT).show();
                        loader.dismiss();

                    }

                }
            });
        }
    }


}