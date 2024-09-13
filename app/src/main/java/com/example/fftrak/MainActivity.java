package com.example.fftrak;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int LOCATION_SETTINGS_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private TextView distanceTextView;
    private DatabaseReference databaseReference;
    private LottieAnimationView scanningAnimation, scanningAnimation1;
    private Button connectButton, findButton;
    private RecyclerView usersRecyclerView;
    private TextView helloWorldTextView;
    private UsersAdapter usersAdapter;
    private UserLocation selectedUser;
    private MediaPlayer mediaPlayer;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        distanceTextView = findViewById(R.id.locationTextView);
        scanningAnimation = findViewById(R.id.scanningAnimation);
        scanningAnimation1 = findViewById(R.id.scanningAnimation2);
        connectButton = findViewById(R.id.connectButton);
        findButton = findViewById(R.id.findButton);
        helloWorldTextView = findViewById(R.id.locationTextView2);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        mediaPlayer = MediaPlayer.create(this, R.raw.rington);
        mediaPlayer.setLooping(true);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersAdapter = new UsersAdapter(new ArrayList<>(), user -> {
            if (!isConnected) {
                selectedUser = user;
                Toast.makeText(MainActivity.this, "Selected User: " + user.getId(), Toast.LENGTH_SHORT).show();
                usersAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(MainActivity.this, "Cannot select another user while connected", Toast.LENGTH_SHORT).show();
            }
        });
        usersRecyclerView.setAdapter(usersAdapter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            checkLocationSettings();
        }

        connectButton.setOnClickListener(v -> toggleConnection());
        findButton.setOnClickListener(v -> findNearbyUsers());
    }

    private void checkLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> startLocationUpdates());

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MainActivity.this, LOCATION_SETTINGS_REQUEST_CODE);
                } catch (IntentSender.SendIntentException sendEx) {
                    Log.d("MainActivity", "Resolution failed: " + sendEx.getMessage());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTINGS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location services are required for this app", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(200);
        locationRequest.setFastestInterval(200);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateLocationToFirebase(location);
                        if (selectedUser != null) {
                            updateDistance(selectedUser);
                        }
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updateLocationToFirebase(Location location) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();

        if (currentUser != null) {
            UserLocation userLocation = new UserLocation(userId, location.getLatitude(), location.getLongitude());
            databaseReference.child(userId).child("location").setValue(userLocation);
            databaseReference.child(userId).child("locationEnabled").setValue(true);
            Toast.makeText(this, "Location is On", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeLocationTracking(String otherUserId) {
        Log.d("MainActivity", "Initializing location tracking for: " + otherUserId);
        databaseReference.child(otherUserId).child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserLocation otherUserLocation = snapshot.getValue(UserLocation.class);
                if (otherUserLocation != null) {
                    selectedUser = otherUserLocation;
                    updateDistance(otherUserLocation);
                } else {
                    Log.d("MainActivity", "Other user location is null.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("MainActivity", "Failed to read other user location: " + error.getMessage());
            }
        });
    }

    private void updateDistance(UserLocation otherUserLocation) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                Location otherLocation = new Location("");
                otherLocation.setLatitude(otherUserLocation.getLatitude());
                otherLocation.setLongitude(otherUserLocation.getLongitude());
                float distance = location.distanceTo(otherLocation);
                distanceTextView.setText(String.format("Distance: %.2f meters", distance));
                if (distance < 500.0) {
                    Toast.makeText(MainActivity.this, "User is within 500 meters", Toast.LENGTH_SHORT).show();
                    scanningAnimation.setVisibility(View.INVISIBLE);
                    scanningAnimation1.setVisibility(View.INVISIBLE);
                    helloWorldTextView.setText("Connected to: " + selectedUser.getId());
                    scanningAnimation.pauseAnimation();
                    scanningAnimation1.pauseAnimation();
                    mediaPlayer.pause();
                }
            }
        });
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void toggleConnection() {
        if (!isConnected) {
            isConnected = true;
            if (selectedUser != null) {
                Toast.makeText(MainActivity.this, "Connected to: " + selectedUser.getId(), Toast.LENGTH_SHORT).show();
                scanningAnimation.setVisibility(View.INVISIBLE);
                scanningAnimation1.setVisibility(View.INVISIBLE);
                helloWorldTextView.setText("Connected to: " + selectedUser.getId());
                scanningAnimation.pauseAnimation();
                scanningAnimation1.pauseAnimation();
                mediaPlayer.pause();
                initializeLocationTracking(selectedUser.getId());
            } else {
                Toast.makeText(MainActivity.this, "Please select a user first", Toast.LENGTH_SHORT).show();
            }
        } else {
            isConnected = false;
            Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            stopLocationUpdates();
            scanningAnimation.setVisibility(View.VISIBLE);
            scanningAnimation1.setVisibility(View.VISIBLE);
            scanningAnimation.playAnimation();
            scanningAnimation1.playAnimation();
            helloWorldTextView.setText("");
            mediaPlayer.start();
        }

    }

    private void findNearbyUsers() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<UserLocation> nearbyUsers = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserLocation userLocation = dataSnapshot.child("location").getValue(UserLocation.class);
                    if (userLocation != null) {
                        nearbyUsers.add(userLocation);
                    }
                }
                usersAdapter.updateUsers(nearbyUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to find nearby users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}