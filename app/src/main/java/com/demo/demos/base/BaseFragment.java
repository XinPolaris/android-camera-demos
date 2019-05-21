package com.demo.demos.base;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangyt on 2019/5/20
 */
public class BaseFragment extends Fragment {
    public static final String TAG = "camerademos";
    public static final int PERMISSION_REQUEST_CAMERA = 0;
    public static final int PERMISSION_REQUEST_STORAGE = 1;

    public static final Map<String, Integer> PERMISSION_CODE_MAP = new HashMap<>();

    static {
        PERMISSION_CODE_MAP.put(Manifest.permission.CAMERA, PERMISSION_REQUEST_CAMERA);
        PERMISSION_CODE_MAP.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkPermission(Manifest.permission.CAMERA);
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void checkPermission(String permission) {
        if (!hasPermission(permission)) {
            getPermissions(permission);
        }
    }

    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void getPermissions(String permission) {
        if (shouldShowRequestPermissionRationale(permission)) {
            showToast("请授予" + permission + "权限");
        } else {
            requestPermissions(new String[]{permission}, PERMISSION_CODE_MAP.get(permission));
        }
    }

    public void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
