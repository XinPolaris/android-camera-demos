package com.demo.demos.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
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
    static int colorMatrixsIndex = 0;

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

    private HandlerThread cameraThread;
    private Handler cameraHandler;


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
        photoSize = outputSizes.get(outputSizes.size() -4);
    }

    private void initViews(View view) {
        btnChangeColor = view.findViewById(R.id.btn_change_color);
        btnChangeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorMatrixsIndex++;
                if (colorMatrixsIndex >= colorMatrixs.size()) {
                    colorMatrixsIndex = 0;
                }
            }
        });

        previewView = view.findViewById(R.id.preview_view);
    }

    @Override
    public void onResume() {
        super.onResume();

        startCameraThread();

        if (previewView.isAvailable()) {
            openCamera();
        } else {
            previewView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        releaseCamera();
        stopCameraThread();
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
                cameraManager.openCamera(cameraId, cameraStateCallback, cameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                Log.d(TAG, "相机访问异常");
            }
        }
    }

    private void createImageReaderAndSurface() {
        previewReader = ImageReader.newInstance(photoSize.getWidth(), photoSize.getHeight(), ImageFormat.JPEG, 2);
        previewReader.setOnImageAvailableListener(
                new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        cameraHandler.post(new PreviewProcessor(getContext(), reader,
                                displayRotation, photoSize, previewView));
                    }
                },
                cameraHandler);
        readerSurface = previewReader.getSurface();
    }

    private void releaseCamera() {
        CameraUtils.getInstance().releaseCameraSession(captureSession);
        CameraUtils.getInstance().releaseCameraDevice(cameraDevice);
    }

    private void startCameraThread() {
        cameraThread = new HandlerThread("cameraThread");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    private void stopCameraThread() {
        cameraThread.quitSafely();
        try {
            cameraThread.join();
            cameraThread = null;
            cameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

                previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                previewRequestBuilder.addTarget(readerSurface);
                previewRequest = previewRequestBuilder.build();

                cameraDevice.createCaptureSession(Arrays.asList(readerSurface), sessionsStateCallback, cameraHandler);
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
                captureSession.setRepeatingRequest(previewRequest, null, cameraHandler);
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

    /************************************* 内部类 ******************************************/
    public static class PreviewProcessor implements Runnable {
        private Context context;
        private ImageReader imageReader;
        private TextureView previewView;
        private int displayRotation, bitmapWidth, bitmapHeight, viewWidth, viewHeight;

        public PreviewProcessor(Context context, ImageReader imageReader, int displayRotation,
                                Size previewSize, TextureView previewView) {
            this.context = context;
            this.imageReader = imageReader;
            this.displayRotation = displayRotation;
            this.previewView = previewView;
            this.bitmapWidth = previewSize.getWidth();
            this.bitmapHeight = previewSize.getHeight();
            this.viewWidth = previewView.getWidth();
            this.viewHeight = previewView.getHeight();
        }

        private Matrix getTransformMatrix() {
            int rotation = PHOTO_ORITATION.get(displayRotation);

            Matrix matrix = new Matrix();

            float dx = (viewWidth - bitmapWidth) / 2;
            float dy = (viewHeight - bitmapHeight) / 2;
            matrix.postTranslate(dx, dy);

            matrix.postRotate(rotation, viewWidth / 2, viewHeight / 2);

            float scaleW = ((float) viewWidth) / bitmapWidth;
            float scaleH = ((float) viewHeight) / bitmapHeight;
            float scale = Math.max(scaleW, scaleH);
            matrix.postScale(scale, scale, viewWidth / 2, viewHeight / 2);
            return matrix;
        }

        private void drawPreviewOutput(Bitmap bitmap) {
            Log.d(TAG, "drawPreviewOutput: 绘制中");
            Canvas canvas = previewView.lockCanvas();
            if (canvas == null) {
                return;
            }
            Paint paint = new Paint();
            paint.setColorFilter(getColorMatrix());
            canvas.drawBitmap(bitmap, 0, 0, paint);
            previewView.unlockCanvasAndPost(canvas);
        }

        private ColorMatrixColorFilter getColorMatrix() {
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.set(colorMatrixs.get(colorMatrixsIndex));
            return new ColorMatrixColorFilter(colorMatrix);
        }

        @Override
        public void run() {
            Image image = imageReader.acquireLatestImage();
            if (image == null) {
                return;
            }
            final ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] data = new byte[buffer.remaining()];
            Log.d(TAG, "data-size=" + data.length / 1024 + "kb");
            buffer.get(data);
            Bitmap origin = BitmapFactory.decodeByteArray(data, 0, data.length);
            final Bitmap newBitmap = Bitmap.createBitmap(origin, 0, 0, origin.getWidth(), origin.getHeight(), getTransformMatrix(), false);
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    drawPreviewOutput(newBitmap);
                }
            });
            image.close();
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
