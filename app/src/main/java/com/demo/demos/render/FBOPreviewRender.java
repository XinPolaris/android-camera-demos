package com.demo.demos.render;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;

import com.demo.demos.R;
import com.demo.demos.filter.BaseFilter;
import com.demo.demos.filter.FBOOesFilter;
import com.demo.demos.utils.GLUtil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES30.*;

/**
 * Created by wangyt on 2019/5/21
 */
public class FBOPreviewRender implements GLSurfaceView.Renderer{

    SurfaceTexture surfaceTexture;
    int[] cameraTexture = new int[1];

    FBOOesFilter fboOesFilter;
    BaseFilter baseFilter;

    public FBOPreviewRender() {
        fboOesFilter = new FBOOesFilter();
        baseFilter = new BaseFilter();
    }

    public SurfaceTexture getSurfaceTexture(){
        return surfaceTexture;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        createTexture();
        surfaceTexture = new SurfaceTexture(cameraTexture[0]);

        fboOesFilter.onSurfaceCreated();
        baseFilter.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        fboOesFilter.onSurfaceChanged(width, height);
        baseFilter.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        fboOesFilter.setTextureId(cameraTexture);
        fboOesFilter.onDraw();
        baseFilter.setTextureId(fboOesFilter.getOutputTextureId());
        baseFilter.onDraw();
    }

    private void createTexture(){
        glGenTextures(cameraTexture.length, cameraTexture, 0);
        //将纹理放到当前单元的 GL_TEXTURE_BINDING_EXTERNAL_OES 目标对象中
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture[0]);
        //设置纹理过滤参数
        glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER,GL_NEAREST);
        glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

}
