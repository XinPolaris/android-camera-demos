package com.demo.demos;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.demos.fragments.FilterFragment;
import com.demo.demos.fragments.GLFilterFragment;
import com.demo.demos.fragments.PhotoFragment;
import com.demo.demos.fragments.PreviewFragment;
import com.demo.demos.utils.CameraUtils;
import com.demo.demos.utils.EGLUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CameraUtils.init(this);
        EGLUtil.init(this);

        if (null == savedInstanceState){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_camera, new GLFilterFragment())
                    .commit();
        }
    }
}
