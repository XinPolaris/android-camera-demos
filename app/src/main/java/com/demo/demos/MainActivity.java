package com.demo.demos;

import android.Manifest;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.demo.demos.base.BaseActivity;
import com.demo.demos.fragments.PhotoFragment;
import com.demo.demos.utils.CameraUtils;
import com.demo.demos.utils.GLUtil;

import java.util.List;

public class MainActivity extends BaseActivity  {

    public static Size photoSize;

    Button btnPreview, btnCapture, btnGLPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CameraUtils.init(this);
        GLUtil.init(this);


        String cameraId = CameraUtils.getInstance().getFrontCameraId();
        List<Size> sizes =  CameraUtils.getInstance().getCameraOutputSizes(cameraId, SurfaceTexture.class);
        String[] list = new String[sizes.size()];
        for (int i = 0; i < sizes.size(); i++) {
            list[i] = sizes.get(i).toString();
        }
        ArrayAdapter starAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setAdapter(starAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                photoSize = sizes.get(i);
                transactFragment(new PhotoFragment());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
//        btnPreview = findViewById(R.id.btnPreview);
//        btnPreview.setOnClickListener(this);
//
//        btnCapture = findViewById(R.id.btnCapture);
//        btnCapture.setOnClickListener(this);
//
//        btnGLPreview = findViewById(R.id.btnGLPreview);
//        btnGLPreview.setOnClickListener(this);

        requestPermission("请给予相机、存储权限，以便app正常工作", null,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btnPreview:
//                transactFragment(new PreviewFragment());
//                break;
//            case R.id.btnCapture:
//                transactFragment(new PhotoFragment());
//                break;
//            case R.id.btnGLPreview:
//                transactFragment(new GLPreviewFragment());
//                break;
//        }
//    }

    private void transactFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container_camera, fragment)
                .commit();
    }
}
