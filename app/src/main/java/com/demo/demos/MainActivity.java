package com.demo.demos;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.demo.demos.fragments.PhotoFragment;
import com.demo.demos.fragments.PreviewFragment;
import com.demo.demos.utils.CameraUtils;
import com.demo.demos.utils.EGLUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnPreview, btnCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CameraUtils.init(this);
        EGLUtil.init(this);

        btnPreview = findViewById(R.id.btnPreview);
        btnPreview.setOnClickListener(this);

        btnCapture = findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnPreview:
                transactFragment(new PreviewFragment());
                break;
            case R.id.btnCapture:
                transactFragment(new PhotoFragment());
                break;
        }
    }

    private void transactFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container_camera,fragment)
                .commit();
    }
}
