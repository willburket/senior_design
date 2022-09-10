package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import com.intel.realsense.librealsense.DeviceListener;
import com.intel.realsense.librealsense.RsContext;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private static final int REQUEST_PERMISSIONS = 1234;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA
    };
    private static final int PERMISSIONS_COUNT = PERMISSIONS.length;
    TextView textViewLocation;

    private RsContext mRsContext;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        // get date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        String currentDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime());
        String currentDateandTime = currentDate + "\nTime: " + currentTime;
        // display date and time
        TextView textViewDate = findViewById(R.id.date);
        textViewDate.setText(currentDateandTime);

        // getting location
        // checking permissions for location and storage first
        textViewLocation = findViewById(R.id.location);
        if (!arePermissionsGranted()) {
            // request permissions if needed
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS,
                    REQUEST_PERMISSIONS
            );
        }
        else {
            getCurrentLocation();
        }



        // for navigating to FileAcvitvity
        Button toCameraSelect = (Button) findViewById(R.id.toCameraSelect);
        toCameraSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            }
        });

        Button toFilePage = (Button) findViewById(R.id.toFilePage);
        toFilePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DirectoryActivity.class));
            }
        });

        Button test = findViewById(R.id.test);            // Add button to begin depth stream
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                // if button is clicked, start new activity

                Intent playbackIntent= new Intent(getApplicationContext(),
                        playback.class);
                startActivity(playbackIntent);             // activity start
            }
        });



        //RsContext.init must be called once in the application's lifetime before any interaction with physical RealSense devices.
        //For multi activities applications use the application context instead of the activity context
        RsContext.init(getApplicationContext());

        printMessage();

        //Register to notifications regarding RealSense devices attach/detach events via the DeviceListener.
        mRsContext = new RsContext();
        mRsContext.setDevicesChangedCallback(new DeviceListener() {
            @Override
            public void onDeviceAttach() {
                printMessage();
            }

            @Override
            public void onDeviceDetach() {
                printMessage();
            }
        });
    }

    // Checks if all permissions have been granted
    private boolean arePermissionsGranted(){
        int p = 0;
        while (p<PERMISSIONS_COUNT) {
            if (checkSelfPermission(PERMISSIONS[p]) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            p++;
        }
        return true;
    }

    // Closes application if permissions have not been granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_PERMISSIONS && grantResults.length>0){
            if (arePermissionsGranted()) {
                getCurrentLocation();
            }  else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
                recreate();
            }
        }
    }

    // Gets current location
    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {

                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                            .removeLocationUpdates(this);
                    if (locationResult != null && locationResult.getLocations().size() > 0) {
                        int i = locationResult.getLocations().size() - 1;
                        double latitude =
                            locationResult.getLocations().get(i).getLatitude();
                        double longitude =
                            locationResult.getLocations().get(i).getLongitude();
                        String currentLocation = String.format("Latitude: %s\nLongitude: %s",
                            latitude,
                            longitude);
                        textViewLocation.setText(currentLocation);
                    }
                }

        }, Looper.getMainLooper());
    }

    private void printMessage(){
        // Example of a call to native methods
        int cameraCount = nGetCamerasCountFromJNI();
        final String version = nGetLibrealsenseVersionFromJNI();
        final String cameraCountString;
        if(cameraCount == 0)
            cameraCountString = "No cameras are currently connected.";
        else
            cameraCountString = "Camera is connected";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView camera_connected = (TextView) findViewById(R.id.camera_connected);
                camera_connected.setText("This app use librealsense: " + version + "\n" + cameraCountString);
            }
        });
    }

    // refresh the page when using the back button
    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    private static native String nGetLibrealsenseVersionFromJNI();
    private static native int nGetCamerasCountFromJNI();
}
