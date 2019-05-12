package com.demo.demos.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by wangyt on 2019/5/9
 */
public class CommonUtil {

    public static final String TAG = "opengl-demos";

    public static boolean checkGLVersion(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ConfigurationInfo ci = am.getDeviceConfigurationInfo();
        return ci.reqGlEsVersion >= 0x30000;
    }

}
