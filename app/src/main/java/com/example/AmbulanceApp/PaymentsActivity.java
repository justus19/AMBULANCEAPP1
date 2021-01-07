package com.example.AmbulanceApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bdhobare.mpesa.Mpesa;
import com.bdhobare.mpesa.interfaces.AuthListener;
import com.bdhobare.mpesa.interfaces.MpesaListener;
import com.bdhobare.mpesa.models.STKPush;
import com.bdhobare.mpesa.utils.Pair;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class PaymentsActivity extends AppCompatActivity implements AuthListener, MpesaListener {


    public static final String BUSINESS_SHORT_CODE = "174379";
    public static final String PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
    public static final String CONSUMER_KEY = "fGg1cSjHn6WurZetyMZXNqFoclbtWOkE";
    public static final String CONSUMER_SECRET = "Gk8OtQf0ZdGXwXKi";
    public static final String CALLBACK_URL = "http://mpess-requestbin.herokuapp.com/loinich1";
    Button payBT;
    TextView priceTV;
    RatingBar ratingBar;
    DatabaseReference historyDatabase;
    String userID;
    String phoneNumber;
    EditText phoneET;
    private ProgressDialog dialog;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static final String  NOTIFICATION = "PushNotification";
    private String historyID;
    private Spinner paymentSP;
    LinearLayout mpesaPayLL;
    Button cashPayBT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        init();

        Mpesa.with(this, CONSUMER_KEY, CONSUMER_SECRET);
        getHistoryID();



        paymentSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String  SpinnerValue = (String) paymentSP.getSelectedItem();

                if (SpinnerValue.equals("Cash")){


                    mpesaPayLL.setVisibility(View.GONE);
                    cashPayBT.setVisibility(View.VISIBLE);

                }else if (SpinnerValue.equals("M-pesa")){

                    mpesaPayLL.setVisibility(View.VISIBLE);
                    cashPayBT.setVisibility(View.GONE);

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        dialog = new ProgressDialog(this);
        dialog.setMessage("Processing");
        dialog.setIndeterminate(true);


        payBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phone = phoneET.getText().toString();
                //String a = String.valueOf(ridePrice);
                //int b = (int) Math.round(ridePrice);

                int c = 1;
                pay(phone, c);

            }
        });


        cashPayBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference patientPayRef = FirebaseDatabase.getInstance().getReference().child("users").child("patient").child(userID).child("latestRidePayment");
                patientPayRef.removeValue();
                showDialog("Transaction successful", "Thank you for choosing "+ R.string.app_name,0);
            }
        });


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(NOTIFICATION)) {
                    String title = intent.getStringExtra("title");
                    String message = intent.getStringExtra("message");
                    int code = intent.getIntExtra("code", 0);
                    showDialog(title, message, code);

                }
            }
        };
    }


    private void getHistoryID() {

        DatabaseReference patientPayRef = FirebaseDatabase.getInstance().getReference().child("users").child("patient").child(userID).child("latestRidePayment");
        patientPayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                if ( snapshot.exists()){

                    Map<String , Object> map = (Map<String , Object>) snapshot.getValue();
                    if (map.get("latestRideDBKEY") != null){

                        historyID = map.get("latestRideDBKEY").toString();
                        historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(historyID);
                        getRidePrice();

                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    private void getRidePrice() {
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                if ( snapshot.exists()){

                    Map<String , Object> map = (Map<String , Object>) snapshot.getValue();
                    if (map.get("destination") != null){

                        Float distance = Float.parseFloat(map.get("destination").toString());
                        Double ridePrice = distance * 0.5;
                        int calculatedridePrice = (int) Math.round(ridePrice);
                        priceTV.setText("Ksh "+ String.valueOf(calculatedridePrice));
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void init() {

        payBT = findViewById(R.id.pay2BT);
        priceTV = findViewById(R.id.ridePriceTV);
        ratingBar = findViewById(R.id.ratingBar);
        //historyID = getHistoryID();
        //  historyDatabase = FirebaseDatabase.getInstance().getReference().child("History").child(historyID);
        phoneET = findViewById(R.id.payNumberET);
        paymentSP =  findViewById(R.id.paymentSP);
        cashPayBT = findViewById(R.id.cashpayBT);
        mpesaPayLL = findViewById(R.id.mpesaPayLL);


        ArrayAdapter<CharSequence> typeofcarAdapter = ArrayAdapter
                .createFromResource(getBaseContext(), R.array.payments, R.layout.spinner_item);
        paymentSP.setAdapter(typeofcarAdapter);

    }



    @Override
    public void onAuthError(Pair<Integer, String> result) {
        Log.e("Error", result.message);
        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onAuthSuccess() {

        //TODO make payment
        payBT.setEnabled(true);
    }


    private void pay(String phone, int amount){
        // dialog.show();
        DatabaseReference patientPayRef = FirebaseDatabase.getInstance().getReference().child("users").child("patient").child(userID).child("latestRidePayment");
        patientPayRef.removeValue();
        STKPush.Builder builder = new STKPush.Builder(BUSINESS_SHORT_CODE, PASSKEY, amount,BUSINESS_SHORT_CODE, phone);
        //builder.setCallBackURL(CALLBACK_URL);
        //SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        //String token = sharedPreferences.getString("InstanceID", "");
        //builder.setFirebaseRegID(token);
        STKPush push = builder.build();
        Mpesa.getInstance().pay(this, push);

        showDialog("Transaction successful", "Thank you for choosing "+ R.string.app_name,0);
    }

    @Override
    public void onMpesaError(Pair<Integer, String> result) {

        dialog.hide();
        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMpesaSuccess(String MerchantRequestID, String CheckoutRequestID, String CustomerMessage) {

        dialog.hide();
        Toast.makeText(this, "successful", Toast.LENGTH_SHORT).show();


    }


    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(NOTIFICATION));

    }
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }


    private void showDialog(String title, String message,int code){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(title)
                .titleGravity(GravityEnum.CENTER)
                .customView(R.layout.success_dialog, true)
                .positiveText("OK")
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        // dialog.dismiss();
                    }
                })
                .build();
        View view=dialog.getCustomView();
        TextView messageText = view.findViewById(R.id.message);
        ImageView imageView = view.findViewById(R.id.success);
        if (code != 0){
            imageView.setVisibility(View.GONE);
        }
        messageText.setText(message);
        dialog.show();
    }


}