package com.example.nativeexample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.intel.realsense.librealsense.DeviceListener;
import com.intel.realsense.librealsense.RsContext;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;             //Check for camera permission

    private RsContext mRsContext;

    // Used to load the 'native-lib' library
    static {
        System.loadLibrary("native-lib");                      //load in cpp library
    }

    @Override                                           // request camera permission
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //RsContext.init must be called once before interaction with physical RealSense devices.
        //For multi activities applications use the application context instead of the activity context
        RsContext.init(getApplicationContext());

        Button distance = findViewById(R.id.distance);            // Add button to begin depth stream
        distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                // if button is clicked, start new activity
               // setContentView(R.layout.activity_activitytwo);
                Intent activity2Intent = new Intent(getApplicationContext(),
                        Activitytwo.class);
                startActivity(activity2Intent);             // activity start
            }
        });

        Button stream = findViewById(R.id.stream);            // Add button to begin streaming video
        stream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                // if button is clicked, start new activity
              //  setContentView(R.layout.activity_main3);
                Intent activity3Intent = new Intent(getApplicationContext(),
                       MainActivity3.class);
                        startActivity(activity3Intent);

                           // activity start
            }
        });

        Button edge = findViewById(R.id.Edge);
        edge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent edgeIntent = new Intent(getApplicationContext(),
                        edgedetect.class);
                        startActivity(edgeIntent);
            }
        });


        final Button capture = findViewById(R.id.capture);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent captureIntent = new Intent(getApplicationContext(),
                        capture.class);
                        startActivity(captureIntent);
            }
        });

        final Button picture = findViewById(R.id.picture);
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pictureIntent = new Intent(getApplicationContext(),
                        picture.class);
                        startActivity(pictureIntent);
            }
        });

        final Button files = findViewById(R.id.files);
        files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent filesIntent = new Intent(getApplicationContext(),
                        files.class);
                startActivity(filesIntent);
            }
        });



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

        // Android 9 also requires camera permissions
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.O &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }



    private void printMessage(){
        // call to native methods
        int cameraCount = nGetCamerasCountFromJNI(); // check if intel camera is connected
        final String version = nGetLibrealsenseVersionFromJNI();    // check realsense library version
        final String cameraCountString;
        if(cameraCount == 0)
            cameraCountString = "No cameras are currently connected.";  // no camera connected message
        else
            cameraCountString = "Camera is connected";      // camera detected message
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView) findViewById(R.id.sample_text);
                tv.setText("This app uses librealsense: " + version + "\n" + cameraCountString); // realsense version message
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    private static native String nGetLibrealsenseVersionFromJNI();
    private static native int nGetCamerasCountFromJNI();
}