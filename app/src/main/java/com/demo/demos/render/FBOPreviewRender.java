package com.demo.demos.render;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;

import com.demo.demos.filter.ColorFilter;
import com.demo.demos.filter.CameraFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES30.*;

/**
 * Created by wangyt on 2019/5/21
 */
public class FBOPreviewRender implements GLSurfaceView.Renderer{

    SurfaceTexture surfaceTexture;
    int[] cameraTexture = new int[1];

    CameraFilter cameraFilter;
    ColorFilter colorFilter;

    public FBOPreviewRender() {
        cameraFilter = new CameraFilter();
        colorFilter = new ColorFilter();
    }

    public SurfaceTexture getSurfaceTexture(){
        return surfaceTexture;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        createTexture();
        surfaceTexture = new SurfaceTexture(cameraTexture[0]);

        cameraFilter.onSurfaceCreated();
        colorFilter.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        cameraFilter.onSurfaceChanged(width, height);
        colorFilter.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (surfaceTexture != null){
            surfaceTexture.updateTexImage();
        }

        cameraFilter.setTextureId(cameraTexture);
        cameraFilter.onDraw();
        colorFilter.setTextureId(cameraFilter.getOutputTextureId());
        colorFilter.onDraw();
    }

    private void createTexture(){
        glGenTextures(cameraTexture.length, cameraTexture, 0);
    }

}
