#include <jni.h>
#include <string>

#include "librealsense2/rs.hpp"



extern "C"
JNIEXPORT jint

JNICALL
Java_com_example_nativeexample_MainActivity_nGetCamerasCountFromJNI(JNIEnv *env, jclass clazz) {
    rs2::context ctx;
    return ctx.query_devices().size();;                 // query number of devices are connected
                                                       // implemented in Main activity
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_example_nativeexample_MainActivity_nGetLibrealsenseVersionFromJNI(JNIEnv *env, jclass clazz) {
    return (*env).NewStringUTF(RS2_API_VERSION_STR);        // query the version of intel realsense API
                                                        // implemented in main activity
}


