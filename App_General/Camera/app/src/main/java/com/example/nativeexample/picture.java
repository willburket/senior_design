package com.example.nativeexample;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.intel.realsense.librealsense.Config;
import com.intel.realsense.librealsense.DeviceList;
import com.intel.realsense.librealsense.DeviceListener;
import com.intel.realsense.librealsense.FrameSet;
import com.intel.realsense.librealsense.GLRsSurfaceView;
import com.intel.realsense.librealsense.Pipeline;
import com.intel.realsense.librealsense.PipelineProfile;
import com.intel.realsense.librealsense.RsContext;
import com.intel.realsense.librealsense.StreamType;

import java.io.File;
import java.nio.channels.Pipe;
import java.text.SimpleDateFormat;
import java.util.Date;

public class picture extends AppCompatActivity {
    private static final String TAG = "librs recording example";
    private static final int PERMISSIONS_REQUEST_CAMERA = 0;
    private static final int PERMISSIONS_REQUEST_WRITE = 1;

    private boolean mPermissionsGranted = true;         //set permissions to true

    private Context mAppContext;
    private TextView mBackGroundText;
    private GLRsSurfaceView mGLSurfaceView;
    private boolean mIsStreaming = false;
    private final Handler mHandler = new Handler();

    private Pipeline mPipeline;
    private RsContext mRsContext;

    private FloatingActionButton mStartRecordFab;
    private FloatingActionButton mStopRecordFab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        mAppContext = getApplicationContext();
        mBackGroundText = findViewById(R.id.connectCameraText);
        mGLSurfaceView = findViewById(R.id.glSurfaceView);

        mStartRecordFab = findViewById(R.id.startRecordFab);
        mStopRecordFab = findViewById(R.id.stopRecordFab);

        mGLSurfaceView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        mStartRecordFab.setOnClickListener(new View.OnClickListener() {         //start recording button
            @Override
            public void onClick(View view) {
                toggleRecording();                                          // toggle recording
            }
        });
        mStopRecordFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleRecording();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGLSurfaceView.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_WRITE);
            return;
        }



        mPermissionsGranted = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mPermissionsGranted)
            init();
        else
            Log.e(TAG, "missing permissions");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mRsContext != null)
            mRsContext.close();
        stop();
    }

    private String getFilePath(){
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "rs_bags");

        folder.mkdir();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateAndTime = sdf.format(new Date());
        File file = new File(folder, currentDateAndTime + ".bag");
        return file.getAbsolutePath();
    }

    void init(){

        RsContext.init(mAppContext);


        mRsContext = new RsContext();
        mRsContext.setDevicesChangedCallback(mListener);

        mPipeline = new Pipeline();

        try(DeviceList dl = mRsContext.queryDevices()){
            if(dl.getDeviceCount() > 0) {
                showConnectLabel(false);
                start(false);
            }
        }
    }

    private void showConnectLabel(final boolean state){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (state){
                    mBackGroundText.setVisibility(View.VISIBLE);
                    mStartRecordFab.hide();
                }
                else{
                    mStartRecordFab.show();
                    mBackGroundText.setVisibility(View.GONE);
                }

                mStopRecordFab.hide();


            }
        });
    }

    private void toggleRecording(){


        if (mStartRecordFab.getVisibility() == View.VISIBLE){
            start(true);
        }
        else {
            stop();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mStartRecordFab.getVisibility() == View.GONE){
                 //   start(true);
                    mStartRecordFab.show();

                }
                else{
                    mStartRecordFab.hide();
                 //   start(true);
                }

                if(mStopRecordFab.getVisibility() == View.GONE){
                    mStopRecordFab.show();
                }
                else{
                    mStopRecordFab.hide();
                }


            }
        });
    }

    private DeviceListener mListener = new DeviceListener() {
        @Override
        public void onDeviceAttach() {
            showConnectLabel(false);
        }

        @Override
        public void onDeviceDetach() {
            showConnectLabel(true);
            stop();
        }
    };


    Runnable mStreaming = new Runnable() {
        @Override
        public void run() {
            try {
                try(FrameSet frames = mPipeline.waitForFrames()) {
                    mGLSurfaceView.upload(frames);
                }
                mHandler.post(mStreaming);
            }
            catch (Exception e) {
                Log.e(TAG, "streaming, error: " + e.getMessage());
            }
        }
    };

    private synchronized void start(boolean record) {
        if(mIsStreaming)
            return;
        try{
            mGLSurfaceView.clear();
            Log.d(TAG, "try start streaming");
            try(Config cfg = new Config()) {
                cfg.enableStream(StreamType.DEPTH, 640, 480);
                cfg.enableStream(StreamType.COLOR, 640, 480);
                if (record)
                    cfg.enableRecordToFile(getFilePath());
                // try statement needed here to release resources allocated by the Pipeline:start() method
                try(PipelineProfile pp = mPipeline.start(cfg)){}
            }
            mIsStreaming = true;
            mHandler.post(mStreaming);
            Log.d(TAG, "streaming started successfully");
        } catch (Exception e) {
            Log.d(TAG, "failed to start streaming");
        }
    }

    private synchronized void stop() {
        if(!mIsStreaming)
            return;
        try {
            Log.d(TAG, "try stop streaming");
            mIsStreaming = false;
            mHandler.removeCallbacks(mStreaming);
            mPipeline.stop();
            mGLSurfaceView.clear();
            Log.d(TAG, "streaming stopped successfully");
        }  catch (Exception e) {
            Log.d(TAG, "failed to stop streaming");
            mPipeline = null;
        }
    }
}