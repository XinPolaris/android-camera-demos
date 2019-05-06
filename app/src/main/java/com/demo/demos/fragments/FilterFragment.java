package com.demo.demos.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.demo.demos.R;
import com.demo.demos.utils.CameraUtils;
import com.demo.demos.views.AutoFitTextureView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FilterFragment extends Fragment {
    private static final String TAG = "FilterFragment";
    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private static final int PERMISSION_REQUEST_STORAGE = 1;

    private static final SparseIntArray PHOTO_ORITATION = new SparseIntArray();

    static {
        PHOTO_ORITATION.append(Surface.ROTATION_0, 90);
        PHOTO_ORITATION.append(Surface.ROTATION_90, 0);
        PHOTO_ORITATION.append(Surface.ROTATION_180, 270);
        PHOTO_ORITATION.append(Surface.ROTATION_270, 180);
    }

    private static final List<float[]> colorMatrixs = new ArrayList<>();
    int colorMatrixsIndex = 0;

    static {
        float[] grayColorMatrix = {
                0.33f, 0.59f, 0.11f, 0f, 0f,
                0.33f, 0.59f, 0.11f, 0f, 0f,
                0.33f, 0.59f, 0.11f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
        };
        colorMatrixs.add(grayColorMatrix);

        float[] reversColorMatrix = {
                -1f, 0f, 0f, 1f, 1f,
                0f, -1f, 0f, 1f, 1f,
                0f, 0f, -1f, 1f, 1f,
                0f, 0f, 0f, 1f, 0f
        };
        colorMatrixs.add(reversColorMatrix);

        float[] oldColorMatrix = {
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
        };
        colorMatrixs.add(oldColorMatrix);
    }

    AutoFitTextureView previewView;
    Button btnChangeColor;

    String cameraId;
    CameraManager cameraManager;
    List<Size> outputSizes;
    Size photoSize;
    CameraDevice cameraDevice;
    CameraCaptureSession captureSession;
    CaptureRequest.Builder previewRequestBuilder;
    CaptureRequest previewRequest;
    Surface previewSurface;
    ImageReader previewReader;
    Surface readerSurface;

    int cameraOritation;
    int displayRotation;

    public FilterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initCamera();

        initViews(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initCamera() {
        cameraManager = CameraUtils.getInstance().getCameraManager();
        cameraId = CameraUtils.getInstance().getBackCameraId();
        outputSizes = CameraUtils.getInstance().getCameraOutputSizes(cameraId, SurfaceTexture.class);
        photoSize = outputSizes.get(0);
    }

    private void initViews(View view) {
        btnChangeColor = view.findViewById(R.id.btn_change_color);
        btnChangeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorMatrixsIndex++;
                if (colorMatrixsIndex >= colorMatrixs.size()){
                    colorMatrixsIndex = 0;
                }
            }
        });

        previewView = view.findViewById(R.id.preview_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (previewView.isAvailable()) {
            openCamera();
        } else {
            previewView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            try {
                displayRotation = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getOrientation();
                if (displayRotation == Surface.ROTATION_0 || displayRotation == Surface.ROTATION_180) {
                    previewView.setAspectRation(photoSize.getHeight(), photoSize.getWidth());
                } else {
                    previewView.setAspectRation(photoSize.getWidth(), photoSize.getHeight());
                }
//                configureTransform(previewView.getWidth(), previewView.getHeight());
                cameraManager.openCamera(cameraId, cameraStateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                Log.d(TAG, "相机访问异常");
            }
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == previewView || null == photoSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, photoSize.getHeight(), photoSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / photoSize.getHeight(),
                    (float) viewWidth / photoSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        previewView.setTransform(matrix);
    }

    private void createImageReaderAndSurface() {
        previewReader = ImageReader.newInstance(photoSize.getWidth(), photoSize.getHeight(), ImageFormat.JPEG, 2);
        previewReader.setOnImageAvailableListener(
                new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Image image = reader.acquireLatestImage();
                        if (image != null) {
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] data = new byte[buffer.remaining()];
                            Log.d(TAG, "yuv-420_888_data-size=" + data.length/1024 + "kb");
                            buffer.get(data);
                            drawPreviewOutput(BitmapFactory.decodeByteArray(data, 0, data.length));
                            image.close();
                        }
                    }
                },
                null);
        readerSurface = previewReader.getSurface();
    }

    private void drawPreviewOutput(Bitmap bitmap) {
        Log.d(TAG, "drawPreviewOutput: 绘制中");
        Canvas canvas = previewView.lockCanvas();
        if (canvas == null) {
            return;
        }
        Paint paint = new Paint();
        paint.setColorFilter(getColorMatrix());
        canvas.drawBitmap(bitmap, getTransformMatrix(bitmap.getWidth(), bitmap.getHeight()), paint);
        previewView.unlockCanvasAndPost(canvas);
    }


    private Matrix getTransformMatrix(int bitmapWidth, int bitmapHeight) {
        int rotation = PHOTO_ORITATION.get(displayRotation);

        Matrix matrix = new Matrix();

        float dx = (previewView.getWidth() - bitmapWidth) / 2;
        float dy = (previewView.getHeight() - bitmapHeight) / 2;
        matrix.postTranslate(dx, dy);

        matrix.postRotate(rotation, previewView.getWidth() / 2, previewView.getHeight() / 2);

        float scaleW = ((float) previewView.getWidth()) / bitmapWidth;
        float scaleH = ((float) previewView.getHeight()) / bitmapHeight;
        float scale = Math.max(scaleW, scaleH);
        matrix.postScale(scale, scale, previewView.getWidth() / 2, previewView.getHeight() / 2);
        return matrix;
    }

    private ColorMatrixColorFilter getColorMatrix() {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(colorMatrixs.get(colorMatrixsIndex));
        return new ColorMatrixColorFilter(colorMatrix);
    }

    private void releaseCamera() {
        CameraUtils.getInstance().releaseCameraSession(captureSession);
        CameraUtils.getInstance().releaseCameraDevice(cameraDevice);
    }

    /********************************** listener/callback **************************************/
    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //启动相机
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.d(TAG, "相机已启动");

            cameraDevice = camera;
            try {
                //初始化预览输出ImageReader 和 reader surface
                createImageReaderAndSurface();

                //初始化预览 Surface
//                SurfaceTexture surfaceTexture = previewView.getSurfaceTexture();
//                if (surfaceTexture == null) {
//                    return;
//                }
//
//                surfaceTexture.setDefaultBufferSize(photoSize.getWidth(), photoSize.getHeight());//设置SurfaceTexture缓冲区大小
//                previewSurface = new Surface(surfaceTexture);

                previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//                previewRequestBuilder.addTarget(previewSurface);
                previewRequestBuilder.addTarget(readerSurface);
                previewRequest = previewRequestBuilder.build();

                cameraDevice.createCaptureSession(Arrays.asList(readerSurface), sessionsStateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                Log.d(TAG, "相机访问异常");
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.d(TAG, "相机已断开连接");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.d(TAG, "相机打开出错");
        }
    };

    CameraCaptureSession.StateCallback sessionsStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            if (null == cameraDevice) {
                return;
            }

            captureSession = session;
            try {
                captureSession.setRepeatingRequest(previewRequest, null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                Log.d(TAG, "相机访问异常");
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            Log.d(TAG, "会话注册失败");
        }
    };

    /************************************* 动态权限 ******************************************/
    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getChildFragmentManager(), "dialog");
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }
    }

    private void requestStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new ConfirmationDialog().show(getChildFragmentManager(), "dialog");
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }
    }

    public static class ConfirmationDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage("请给予相机、文件读写权限")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    PERMISSION_REQUEST_CAMERA);
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
}
