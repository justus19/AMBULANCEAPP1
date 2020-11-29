package com.example.driveapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Button nDriver, nRescue;
    private FirebaseAuth.AuthStateListener fireBaseAuthListener;
    DatabaseReference userDatabaseRef;
    private FirebaseAuth mAuth;
    boolean loggedIn=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        fireBaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {

                    loggedIn = true;

                }else {

                    loggedIn = false;
                }
            }
        };

        Thread background = new Thread() {

            public void run() {
                try {

                    sleep(3*1000);

                    if (loggedIn == true){


                        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");
                        String userId = mAuth.getUid();

                        userDatabaseRef.child(userId)
                                .addValueEventListener(new ValueEventListener() {

                                    String accountType;
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                                            if (map.get("type") != null) {

                                                accountType = map.get("type").toString();

                                            }


                                            if (accountType.equals("driver")) {

                                                Toast.makeText(getBaseContext(),"Welcome Driver",Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent(getBaseContext(), DriverMapActivity.class);
                                                startActivity(intent);
                                                finish();
                                                return;


                                            } else if (accountType.equals("rescue")) {
                                                Toast.makeText(getBaseContext(),"Welcome Rescue",Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent(getBaseContext(), RescueMapsActivity.class);
                                                startActivity(intent);
                                                finish();
                                                return;



                                            }
                                        }else {

                                            Toast.makeText(getBaseContext(),"Account not found!!",Toast.LENGTH_SHORT).show();

                                            FirebaseAuth.getInstance().signOut();
                                            Intent logout=new Intent(getBaseContext(), LoginActivity.class);
                                            logout.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(logout);
                                            Toast.makeText(getBaseContext(),"Login Again!!",Toast.LENGTH_SHORT).show();
                                        }
                                    }


                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {


                                    }
                                });


                    }else {

                        Intent goToLogin = new Intent(getBaseContext(), LoginActivity.class);
                        startActivity(goToLogin);
                        finish();
                    }


                } catch (Exception e) {

                }
            }
        };

        mAuth.addAuthStateListener(fireBaseAuthListener);
        background.start();

    }


    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(fireBaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(fireBaseAuthListener);
    }


}