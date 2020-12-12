package com.example.driveapp;

import androidx.fragment.app.FragmentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RescueMapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {
    private GoogleMap mMap;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private Button mLogout, mSettings, mRideStatus, mHistory;
    private Switch mWorkingSwitch;
    private int status = 0;
    private String driverId = "", destination;
    private LatLng destinationLatLng, pickupLatLng;
    private float rideDistance;
    private Boolean isLoggingOut = false;
    private SupportMapFragment mapFragment;

    private LinearLayout driverInfo;
    private ImageView mdriverProfileImage;
    private TextView mdriverName, mdriverPhone, mdriverDestination;
    private List<Polyline> polylines;
    Marker pickupMarker;
    private DatabaseReference assigneddriverPickupLocationRef;
    private ValueEventListener assigneddriverPickupLocationRefListener;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    private double wayLatitude = 0.0, wayLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rescue_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        polylines = new ArrayList<>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        driverInfo = findViewById(R.id.driverInfo);
        mdriverProfileImage = findViewById(R.id.driverProfileImage);
        mdriverName = findViewById(R.id.driverName);
        mdriverPhone = findViewById(R.id.driverPhone);
        mdriverDestination = findViewById(R.id.driverDestination);
        mWorkingSwitch = findViewById(R.id.workingSwitch);
        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                    connectrescue();
                }else{
                    disconnectrescue();
                }
            }
        });

        mSettings = findViewById(R.id.settings);
        mLogout = findViewById(R.id.logout);
        mRideStatus = findViewById(R.id.rideStatus);
        mHistory = findViewById(R.id.history);
        mRideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(status){
                    case 1:
                        status=2;
                        erasePolylines();
                        if(destinationLatLng.latitude!=0.0 && destinationLatLng.longitude!=0.0){
                            getRouteToMarker(destinationLatLng);
                        }
                        mRideStatus.setText("drive completed");

                        break;
                    case 2:
                        recordRide();
                        endRide();
                        break;
                }
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingOut = true;
                disconnectrescue();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(RescueMapsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RescueMapsActivity.this, RescueSettingActivity.class);
                startActivity(intent);
                return;
            }
        });
        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RescueMapsActivity.this, HistoryActivity.class);
                intent.putExtra("rescueOrdriver", "rescue");
                startActivity(intent);
                return;
            }
        });
        getAssigneddriver();
    }
    LocationCallback  mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Toast.makeText(RescueMapsActivity.this, "inside mLocationCallback*********", Toast.LENGTH_SHORT).show();
            for(final Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){

                    if(!driverId.equals("") && mLastLocation!=null && location != null){
                        rideDistance += mLastLocation.distanceTo(location)/1000;
                    }
                    mLastLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(21));

                    final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("rescueAvailable");
                    final DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("rescueWorking");
                    final GeoFire geoFireAvailable = new GeoFire(refAvailable);
                    final GeoFire geoFireWorking = new GeoFire(refWorking);

                    switch (driverId){
                        case "":
                            refWorking.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()  ){
                                        geoFireWorking.removeLocation(userId, new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {
                                                Toast.makeText(RescueMapsActivity.this, "sucessfully removed "+ userId, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    Toast.makeText(RescueMapsActivity.this, "Completed", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;

                        default:
                            refAvailable.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        geoFireAvailable.removeLocation(userId, new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {
                                                Toast.makeText(RescueMapsActivity.this, "sucessfully removed "+ userId, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    Toast.makeText(RescueMapsActivity.this, "Completed", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                    }
                }
            }
        }
    };
    private void getAssigneddriver(){
        Toast.makeText(RescueMapsActivity.this, "inside getAssigneddriver", Toast.LENGTH_SHORT).show();
        final String rescueId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assigneddriverRef = FirebaseDatabase.getInstance().getReference().child("users").child("rescue").child(rescueId).child("driverRequest").child("driverRideId");
        assigneddriverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    status = 1;
                    driverId = dataSnapshot.getValue().toString();
                    getAssigneddriverPickupLocation();
                    getAssigneddriverDestination();
                    getAssigneddriverInfo();
                }else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getRouteToMarker(LatLng pickupLatLng) {
        if (pickupLatLng != null && mLastLocation != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickupLatLng)
                    .build();
            routing.execute();
        }
    }

    private void getAssigneddriverPickupLocation(){
        Toast.makeText(RescueMapsActivity.this, "inside getAssigneddriverPickupLocation", Toast.LENGTH_SHORT).show();

        assigneddriverPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("driverRequest").child(driverId).child("l");
        assigneddriverPickupLocationRefListener = assigneddriverPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !driverId.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat,locationLng);
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_rescue)));
                    getRouteToMarker(pickupLatLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getAssigneddriverDestination(){
        Toast.makeText(RescueMapsActivity.this, "inside getAssignedDestination", Toast.LENGTH_SHORT).show();
        String rescueId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assigneddriverRef = FirebaseDatabase.getInstance().getReference().child("users").child("rescue").child("rescueId").child("driverRequest");
        assigneddriverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("destination")!=null){
                        destination = map.get("destination").toString();
                        mdriverDestination.setText("Destination: " + destination);
                    }
                    else{
                        mdriverDestination.setText("Destination: --");
                    }

                    Double destinationLat = 0.0;
                    Double destinationLng = 0.0;
                    if(map.get("destinationLat") != null){
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }
                    if(map.get("destinationLng") != null){
                        destinationLng = Double.valueOf(map.get("destinationLng").toString());
                        destinationLatLng = new LatLng(destinationLat, destinationLng);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getAssigneddriverInfo(){
        Toast.makeText(RescueMapsActivity.this, "inside getAssigneddriverInfo", Toast.LENGTH_SHORT).show();
        driverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mdriverDatabase = FirebaseDatabase.getInstance().getReference().child("users").child("driver").child(driverId);
        mdriverDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mdriverName.setText(map.get("name").toString());
                    }
                    if(map.get("phone")!=null){
                        mdriverPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("profileImageUrl")!=null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mdriverProfileImage);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void recordRide(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rescueRef = FirebaseDatabase.getInstance().getReference().child("users").child("rescue").child(userId).child("history");
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("users").child("driver").child(driverId).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
        String requestId = historyRef.push().getKey();
        rescueRef.child(requestId).setValue(true);
        driverRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("rescue", userId);
        map.put("driver", driverId);
        map.put("rating", 0);
        map.put("timestamp", getCurrentTimestamp());
        map.put("destination", destination);
        map.put("location/from/lat", pickupLatLng.latitude);
        map.put("location/from/lng", pickupLatLng.longitude);
        map.put("location/to/lat", destinationLatLng.latitude);
        map.put("location/to/lng", destinationLatLng.longitude);
        map.put("distance", rideDistance);
        historyRef.child(requestId).updateChildren(map);
    }
    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }

    private void endRide(){
        Toast.makeText(RescueMapsActivity.this, "inside endRide", Toast.LENGTH_SHORT).show();
        mRideStatus.setText("picked driver");
        erasePolylines();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference rescueRef = FirebaseDatabase.getInstance().getReference().child("users").child("rescue").child(userId).child("driverRequest");

        rescueRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount()>0){
                    rescueRef.removeValue();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driverRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(driverId);
                    driverId="";
                    rideDistance = 0;

                    if(pickupMarker != null){
                        pickupMarker.remove();
                    }
                    if (assigneddriverPickupLocationRefListener != null){
                        assigneddriverPickupLocationRef.removeEventListener(assigneddriverPickupLocationRefListener);
                    }
                    driverInfo.setVisibility(View.GONE);
                    mdriverName.setText("");
                    mdriverPhone.setText("");
                    mdriverDestination.setText("Destination: --");
                    mdriverProfileImage.setImageResource(R.mipmap.ic_launcher);
                }else{
                    Toast.makeText(RescueMapsActivity.this, "No Request found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void connectrescue(){
        checkLocationPermission();
        Toast.makeText(RescueMapsActivity.this, "inside connectrescue", Toast.LENGTH_SHORT).show();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper() );
        mMap.setMyLocationEnabled(true);
    }
    private void disconnectrescue(){
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Toast.makeText(RescueMapsActivity.this, "inside disconnectrescue", Toast.LENGTH_SHORT).show();
        }

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rescueAvailable");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() ){
                    GeoFire geoFire = new GeoFire(ref);
                    //geoFire.removeLocation(userId);
                    geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            Toast.makeText(RescueMapsActivity.this, "suceessfully removed from available list "+ userId, Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    Toast.makeText(RescueMapsActivity.this, "rescue not found in the records", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //drain alot of battery

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//                Toast.makeText(DriverMapActivity.this, "everything is good", Toast.LENGTH_SHORT).show();
//              mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
//              mMap.setMyLocationEnabled(true);
//               Toast.makeText(RescueMapsActivity.this, "inside requestLocationUpdates", Toast.LENGTH_SHORT).show();

            }else{
                checkLocationPermission();
            }
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));


            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void checkLocationPermission() {
        Toast.makeText(RescueMapsActivity.this, "inside checkLocationPermission", Toast.LENGTH_SHORT).show();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(RescueMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else{
                ActivityCompat.requestPermissions(RescueMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Toast.makeText(RescueMapsActivity.this, "inside onRequestPermissionsResult", Toast.LENGTH_SHORT).show();

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);


                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }

}
