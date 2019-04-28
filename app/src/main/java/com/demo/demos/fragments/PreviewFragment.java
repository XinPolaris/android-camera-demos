package com.demo.demos.fragments;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.demo.demos.R;
import com.demo.demos.utils.CameraUtils;
import com.demo.demos.views.AutoFitTextureView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreviewFragment extends Fragment {

    private static final String TAG = "PreviewFragment";
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final long PREVIEW_SIZE_MIN = 720 * 480;

    Button btnChangePreviewSize;
    Button btnImageMode;
    Button btnVideoMode;
//    TextureView previewView;//相机预览view
    AutoFitTextureView previewView;//自适应相机预览view

    CameraManager cameraManager;//相机管理类
    CameraDevice cameraDevice;//相机设备类
    CameraCaptureSession cameraCaptureSession;//相机会话类

    String cameraId;//相机id

    List<Size> outputSizes;//相机输出尺寸
    int sizeIndex = 0;

    Size previewSize;//预览尺寸

    public PreviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //初始化相机
        initCamera();
        //初始化界面
        initViews(view);
    }

    private void initCamera(){
        cameraManager = CameraUtils.getInstance().getCameraManager();
        cameraId = CameraUtils.getInstance().getCameraId(false);//默认使用后置相机
        //获取指定相机的输出尺寸列表，并降序排序
        outputSizes = CameraUtils.getInstance().getCameraOutputSizes(cameraId, SurfaceTexture.class);
        Collections.sort(outputSizes, new Comparator<Size>() {
            @Override
            public int compare(Size o1, Size o2) {
                return o1.getWidth() * o1.getHeight() - o2.getWidth() * o2.getHeight();
            }
        });
        Collections.reverse(outputSizes);
        //初始化预览尺寸
        previewSize = outputSizes.get(0);
    }

    private void initViews(View view){
        btnChangePreviewSize = view.findViewById(R.id.btn_change_preview_size);
        btnChangePreviewSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换预览分辨率
                updateCameraPreview();
                previewView.setAspectRation(previewSize.getWidth(), previewSize.getHeight());
                setButtonText();
            }
        });
        setButtonText();

        btnImageMode = view.findViewById(R.id.btn_image_mode);
        btnImageMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //拍照模式，选择最大输出尺寸
                updateCameraPreviewWithImageMode();
            }
        });

        btnVideoMode = view.findViewById(R.id.btn_video_mode);
        btnVideoMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //录像模式，选择宽高比和预览窗口宽高比最接近且的输出尺寸
                //如果该输出尺寸过小，则选择和预览窗口面积最接近的输出尺寸
                updateCameraPreviewWithVideoMode();
            }
        });

        previewView = view.findViewById(R.id.afttv_camera);
        previewView.setAspectRation(previewSize.getWidth(), previewSize.getHeight());
    }

    @Override
    public void onResume() {
        super.onResume();

        //设置 TextureView 的状态监听
        previewView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

            //TextureView 可用时调用改回调方法
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                //TextureView 可用，打开相机
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    private void openCamera() {
        //申请相机权限
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        try {
            //打开相机
            cameraManager.openCamera(cameraId,
                    new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(CameraDevice camera) {
                            cameraDevice = camera;
                            //创建相机预览 session
                            createPreviewSession();
                        }

                        @Override
                        public void onDisconnected(CameraDevice camera) {
                            //释放相机资源
                            CameraUtils.getInstance().releaseCamera(camera);
                        }

                        @Override
                        public void onError(CameraDevice camera, int error) {
                            //释放相机资源
                            CameraUtils.getInstance().releaseCamera(camera);
                        }
                    },
                    null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createPreviewSession() {
        //根据TextureView 和 选定的 previewSize 创建用于显示预览数据的Surface
        SurfaceTexture surfaceTexture = previewView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());//设置SurfaceTexture缓冲区大小
        final Surface previewSurface = new Surface(surfaceTexture);

        try {
            //创建预览session
            cameraDevice.createCaptureSession(Arrays.asList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {

                            cameraCaptureSession = session;

                            try {
                                //构建预览捕获请求
                                CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                builder.addTarget(previewSurface);//设置 previewSurface 作为预览数据的显示界面
                                CaptureRequest captureRequest = builder.build();
                                //设置重复请求，以获取连续预览数据
                                session.setRepeatingRequest(captureRequest, new CameraCaptureSession.CaptureCallback() {
                                            @Override
                                            public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                                                super.onCaptureProgressed(session, request, partialResult);
                                            }

                                            @Override
                                            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                                super.onCaptureCompleted(session, request, result);
                                            }
                                        },
                                        null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {

                        }
                    },
                    null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void updateCameraPreview(){
        if (sizeIndex + 1 < outputSizes.size()){
            sizeIndex++;
        }else {
            sizeIndex = 0;
        }
        previewSize = outputSizes.get(sizeIndex);
        //重新创建会话
        createPreviewSession();
    }

    private void updateCameraPreviewWithImageMode(){
        previewSize = outputSizes.get(0);
        Log.d(TAG, "img_mode: " + previewSize.getWidth() + "-" + previewSize.getHeight());
        createPreviewSession();
    }

    private void updateCameraPreviewWithVideoMode(){
        List<Size> sizes = new ArrayList<>();
        float ratio = ((float) previewView.getWidth() / previewView.getHeight());
        //首先选取宽高比与预览窗口一致且最大的输出尺寸
        for (int i = 0; i < outputSizes.size(); i++){
            if (((float)outputSizes.get(i).getWidth()) / outputSizes.get(i).getHeight() == ratio){
                sizes.add(outputSizes.get(i));
            }
        }
        if (sizes.size() > 0){
            previewSize = Collections.max(sizes, new CompareSizesByArea());
            Log.d(TAG, "video_mode: " + previewSize.getWidth() + "-" + previewSize.getHeight());
            createPreviewSession();
            return;
        }
        //如果不存在宽高比与预览窗口宽高比一致的输出尺寸，则选择与其宽高比最接近的输出尺寸
        sizes.clear();
        float detRatioMin = Float.MAX_VALUE;
        for (int i = 0; i < outputSizes.size(); i++){
            Size size = outputSizes.get(i);
            float curRatio = ((float)size.getWidth()) / size.getHeight();
            if (Math.abs(curRatio - ratio) < detRatioMin){
                detRatioMin = curRatio;
                previewSize = size;
            }
        }
        Log.d(TAG, "video_mode: " + previewSize.getWidth() + "-" + previewSize.getHeight());
        if (previewSize.getWidth() * previewSize.getHeight() > PREVIEW_SIZE_MIN){
            createPreviewSession();
        }
        //如果宽高比最接近的输出尺寸太小，则选择与预览窗口面积最接近的输出尺寸
        long area = previewView.getWidth() * previewView.getHeight();
        long detAreaMin = Long.MAX_VALUE;
        for (int i = 0; i < outputSizes.size(); i++){
            Size size = outputSizes.get(i);
            long curArea = size.getWidth() * size.getHeight();
            if (Math.abs(curArea - area) < detAreaMin){
                detAreaMin = curArea;
                previewSize = size;
            }
        }
        Log.d(TAG, "video_mode: " + previewSize.getWidth() + "-" + previewSize.getHeight());
        createPreviewSession();
    }

    private void setButtonText(){
        btnChangePreviewSize.setText(previewSize.getWidth() + "-" + previewSize.getHeight());
    }

    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /******************************** 权限/对话框 ************************************************/

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance("请给予相机权限")
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    public static class ConfirmationDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage("请给予相机权限")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = parent.getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
        }
    }

    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
