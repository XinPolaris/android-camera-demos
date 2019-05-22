package com.demo.demos.render;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;

import com.demo.demos.R;
import com.demo.demos.utils.GLUtil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES30.*;

/**
 * Created by wangyt on 2019/5/21
 */
public class GLRender implements GLSurfaceView.Renderer{

    private TexProcess texProcess;
    private TexShow texShow;

    public GLRender() {
        texProcess = new TexProcess();
        texShow = new TexShow();
    }

    public SurfaceTexture getSurfaceTexture(){
        return texProcess.getSurfaceTexture();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        texProcess.onSurfaceCreated();

        texShow.setTextureId(texProcess.getOutputTexture());
        texShow.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        texProcess.onSurfaceChanged(width, height);
        texShow.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        texProcess.onDraw();

        texShow.onDraw();
    }
}
