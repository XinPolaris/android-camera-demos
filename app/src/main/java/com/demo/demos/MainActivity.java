package com.demo.demos;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.demos.fragments.PhotoFragment;
import com.demo.demos.fragments.PreviewFragment;
import com.demo.demos.utils.CameraUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CameraUtils.init(this);

        if (null == savedInstanceState){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_camera, new PhotoFragment())
                    .commit();
        }
    }
}
