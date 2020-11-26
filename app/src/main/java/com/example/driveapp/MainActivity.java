package com.example.driveapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button nDriver, nRescue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nDriver = findViewById(R.id.driver);
        nRescue = findViewById(R.id.rescue);

        nDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DriverRegisterActivity.class);
                startActivity(intent);
                finish();
                return;
            }

        });
        nRescue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RescueRegisterActivity.class);
                startActivity(intent);
                finish();
                return;

            }
        });
    }
}