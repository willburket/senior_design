package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.intel.realsense.librealsense.Config;
import com.intel.realsense.librealsense.DeviceList;
import com.intel.realsense.librealsense.DeviceListener;
import com.intel.realsense.librealsense.FrameSet;
import com.intel.realsense.librealsense.GLRsSurfaceView;
import com.intel.realsense.librealsense.Pipeline;
import com.intel.realsense.librealsense.PipelineProfile;
import com.intel.realsense.librealsense.RsContext;
import com.intel.realsense.librealsense.StreamFormat;
import com.intel.realsense.librealsense.StreamType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoActivity extends AppCompatActivity {
    // GlobalClass variable
    GlobalClass globalClass;

    private static final String TAG = "librs recording example";
    private static final int PERMISSIONS_REQUEST_CAMERA = 0;
    private static final int PERMISSIONS_REQUEST_WRITE = 1;

    private boolean mPermissionsGranted = false;

    private Context mAppContext;
    private TextView mBackGroundText;
    private GLRsSurfaceView mGLSurfaceView;
    private boolean mIsStreaming = false;
    private final Handler mHandler = new Handler();

    private Pipeline mPipeline;
    private RsContext mRsContext;

    private FloatingActionButton mStartRecordFab;
    private FloatingActionButton mStopRecordFab;

    String rootPath;
    int clicked;

    int i;
    // resolution variables
    int depth_height, depth_width, RGB_height, RGB_width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        // get camera settings
        globalClass = (GlobalClass) getApplicationContext();
        // get depth resolution settings
        String depth_res = globalClass.getDepth_res();
        String[] depth_tokens = depth_res.split("x");
        depth_width = Integer.parseInt(depth_tokens[0]);
        depth_height = Integer.parseInt(depth_tokens[1]);
        // get RGB resolution settings
        String RGB_res = globalClass.getRGB_res();
        String[] RGB_tokens = RGB_res.split("x");
        RGB_width = Integer.parseInt(RGB_tokens[0]);
        RGB_height = Integer.parseInt(RGB_tokens[1]);


        rootPath = getIntent().getStringExtra("ROOT");

        mAppContext = getApplicationContext();
        mBackGroundText = findViewById(R.id.connectCameraText);
        mGLSurfaceView = findViewById(R.id.glSurfaceView);

        mStartRecordFab = findViewById(R.id.startRecordFab);
        mStopRecordFab = findViewById(R.id.stopRecordFab);

        mStartRecordFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleRecording();
                clicked = 1;
            }
        });
        mStopRecordFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleRecording();
                clicked = 0;
            }
        });

        // Android 9 also requires camera permissions
       /* if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.O &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
            return;
        }*/

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }

        mPermissionsGranted = true;
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
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
                    // external storage (line 1)
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "rs_bags");
      //  File folder = new File(rootPath);                       // saves in app internal storage
        folder.mkdir();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateAndTime = sdf.format(new Date());
        File file = new File(folder, currentDateAndTime + ".bag");
        return file.getAbsolutePath();

    }

    void init(){
        //RsContext.init must be called once in the application lifetime before any interaction with physical RealSense devices.
        //For multi activities applications use the application context instead of the activity context
        RsContext.init(mAppContext);

        //Register to notifications regarding RealSense devices attach/detach events via the DeviceListener.
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
                mBackGroundText.setVisibility(state ? View.VISIBLE : View.GONE);
                // mStartRecordFab.show(!state ? View.VISIBLE : View.GONE);
                if(state){
                    mStartRecordFab.hide();
                }
                else {
                    mStartRecordFab.show();
                }
                mStopRecordFab.hide();

            }
        });
    }

    private void toggleRecording(){

    /*    if(mStartRecordFab.getVisibility() == View.VISIBLE) {
            start(true);
        }
        else {
            Toast.makeText(VideoActivity.this, "saved", Toast.LENGTH_SHORT).show();
            stop();
        }*/
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(mStartRecordFab.getVisibility() == View.GONE){
                    mStartRecordFab.show();
                    stop();
                    Toast.makeText(VideoActivity.this, "saved", Toast.LENGTH_SHORT).show();
                }
                else {
                    mStartRecordFab.hide();
                    start(true);
                }
                if(mStopRecordFab.getVisibility() == View.GONE){
                    mStopRecordFab.show();
                }
                else {
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
                int b = 0;
                cfg.enableStream(StreamType.DEPTH, depth_width, depth_height);
                cfg.enableStream(StreamType.COLOR, RGB_width, RGB_height);
                //cfg.enableStream(StreamType.COLOR,0,640,480,StreamFormat.RGB8,6);
                if (record)
                   i = 1;
                    cfg.enableRecordToFile(getFilePath());

                // try statement needed here to release resources allocated by the Pipeline:start() method

                try(PipelineProfile pp = mPipeline.start(cfg)){
                    /*while(i==1) {
                        FileOutputStream fileOutputStream = new FileOutputStream(getFilePath());
                        fileOutputStream.write(b);
                        fileOutputStream.flush();
                        b++;
                    }*/
                }


               /* int b = 0;
                while(record){
                fileOutputStream.write(b);
                b++;
                }*/
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
            i = 0;
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