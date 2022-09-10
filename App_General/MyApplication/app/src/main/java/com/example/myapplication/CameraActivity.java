package com.example.myapplication;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity {
    // GlobalClass variable
    GlobalClass globalClass;
    // Spinner variables
    Spinner Depth_res;
    Spinner Depth_FPS;
    Spinner RGB_res;
    Spinner RGB_FPS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.select_camera);

        // setting up all the spinners
        setupSpinners();

        globalClass = (GlobalClass) getApplicationContext();

        Button configure_camera = (Button) findViewById(R.id.configure_camera);
        configure_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Depth_res.getSelectedItem()!=null)
                    globalClass.setDepth_res(Depth_res.getSelectedItem().toString());
                if (RGB_res.getSelectedItem()!=null)
                    globalClass.setRGB_res(RGB_res.getSelectedItem().toString());
                Toast.makeText(CameraActivity.this, "Camera Configured", Toast.LENGTH_SHORT).show();
            }
        });
}


    // Checks if all permissions have been granted
    private void setupSpinners() {
        //Depth Resoltuion
        Depth_res = (Spinner) findViewById(R.id.Depth_res);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.depth_res, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Depth_res.setAdapter(adapter);
        //Depth FPS
        Depth_FPS = (Spinner) findViewById(R.id.Depth_FPS);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.FPS, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Depth_FPS.setAdapter(adapter1);

        //RGB Resoltuion
        RGB_res = (Spinner) findViewById(R.id.RGB_res);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.RGB_res, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        RGB_res.setAdapter(adapter2);
        //RGB FPS
        RGB_FPS = (Spinner) findViewById(R.id.RGB_FPS);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this,
                R.array.FPS, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        RGB_FPS.setAdapter(adapter3);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }
}