package com.example.driveapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DriverMapActivity extends AppCompatActivity {

    Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        logout = findViewById(R.id.logout1);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logout=new Intent(getBaseContext(), LoginActivity.class);
                logout.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logout);
                Toast.makeText(getBaseContext(),"Loged out",Toast.LENGTH_SHORT).show();
            }
        });

        Toast.makeText(getBaseContext(),"Driver maps",Toast.LENGTH_LONG).show();
    }
}