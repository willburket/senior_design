package com.example.myapplication;

import android.app.Application;
import android.widget.Toast;

public class GlobalClass extends Application {
    private String depth_res;
    private String RGB_res;

    public String getDepth_res() {
        if (depth_res==null) return "640x480";
        return depth_res;
    }

    public void setDepth_res(String depth_res) {
        this.depth_res = depth_res;
    }

    public String getRGB_res() {
        if (depth_res==null) return "1280x780";
        return RGB_res;
    }

    public void setRGB_res(String rgb_res) {
        this.RGB_res = rgb_res;
    }

}
