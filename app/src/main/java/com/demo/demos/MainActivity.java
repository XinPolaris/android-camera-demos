package com.demo.demos;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.demos.fragments.PreviewFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (null == savedInstanceState){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_camera, new PreviewFragment())
                    .commit();
        }
    }
}
