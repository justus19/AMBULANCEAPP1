package com.example.driveapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.driveapp.HistoryRecyclerView.HistoryAdapter;
import com.example.driveapp.HistoryRecyclerView.HistoryObject;
import com.example.driveapp.HistoryRecyclerView.HistoryObject;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;



public class HistoryActivity extends AppCompatActivity {
    private String rescueOrdriver, userId;

    private RecyclerView mHistoryRecyclerView;
    private RecyclerView.Adapter mHistoryAdapter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);


        mHistoryRecyclerView = (RecyclerView) findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new HistoryAdapter(getDataSetHistory(), HistoryActivity.this);
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);


        rescueOrdriver = getIntent().getExtras().getString("rescueOrdriver");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();
    }



        private void getUserHistoryIds() {
            DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(rescueOrdriver).child(userId).child("History");
            userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot history : dataSnapshot.getChildren()) {
                            FetchRideInformation(history.getKey());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }


        private void FetchRideInformation (String rideKey){
            DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
            historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String rideId = dataSnapshot.getKey();
                        Long timestamp = 0L;
                        String distance = "";


                        if (dataSnapshot.child("timestamp").getValue() != null) {
                            timestamp = Long.valueOf(dataSnapshot.child("timestamp").getValue().toString());
                        }


                        HistoryObject obj = new HistoryObject(rideId, getDate(timestamp));
                        resultsHistory.add(obj);
                        mHistoryAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        private String getDate (Long time){
            Calendar cal = Calendar.getInstance(Locale.getDefault());
            cal.setTimeInMillis(time * 1000);
            String date = DateFormat.format("MM-dd-yyyy hh:mm", cal).toString();
            return date;
        }

        private ArrayList resultsHistory = new ArrayList<HistoryObject>();
        private ArrayList<HistoryObject> getDataSetHistory () {
            return resultsHistory;
        }
    }
