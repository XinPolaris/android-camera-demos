package com.demo.demos.filter;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.demo.demos.utils.CommonUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static android.opengl.GLES30.*;

/**
 * Created by wangyt on 2019/5/24
 */
public class CameraFilter extends OesFilter{

    //后置相机，顺时针旋转90度
    public static final float[] textureCoordCameraBack = {
            1.0f,1.0f,
            0.0f,1.0f,
            0.0f,0.0f,
            1.0f,0.0f
    };

    public static boolean requestTakePhoto = false;

    public int[] frameBuffer = new int[1];
    public int[] frameTexture = new int[1];

    public CameraFilter() {
        super();
    }

    @Override
    public void initBuffer() {
        vertexBuffer = CommonUtil.getFloatBuffer(vertex);
        textureCoordBuffer = CommonUtil.getFloatBuffer(textureCoordCameraBack);
    }

    @Override
    public int[] getOutputTextureId() {
        return frameTexture;
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        if(this.width!=width||this.height!=height){
            this.width = width;
            this.height = height;
            delFrameBufferAndTexture();
            genFrameBufferAndTexture();
        }
    }

    @Override
    public void onDraw() {
        bindFrameBufferAndTexture();
        super.onDraw();

        if (requestTakePhoto){
            final ByteBuffer photoBuffer = ByteBuffer.allocate(width * height * 4);
            glReadPixels(0, 0, width, height,
                    GL_RGBA, GL_UNSIGNED_BYTE, photoBuffer);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) ;
                    bitmap.copyPixelsFromBuffer(photoBuffer);
                    String folderPath = Environment.getExternalStorageDirectory() + "/DCIM/Camera/";
                    File folder = new File(folderPath);
                    if (!folder.exists() && !folder.mkdirs()){
                        Log.e("demos", "图片目录异常");
                        return;
                    }
                    String filePath = folderPath+System.currentTimeMillis()+".jpg";
                    BufferedOutputStream bos = null;
                    try {
                        FileOutputStream fos = new FileOutputStream(filePath);
                        bos = new BufferedOutputStream(fos);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        if (bos !=null) {
                            try {
                                bos.flush();
                                bos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (bitmap != null){
                            bitmap.recycle();
                        }
                    }
                }
            }).start();
            requestTakePhoto = false;
        }
        unBindFrameBuffer();
    }

    public void delFrameBufferAndTexture(){
        glDeleteFramebuffers(frameBuffer.length, frameBuffer, 0);
        glDeleteTextures(frameTexture.length, frameTexture, 0);
    }

    public void genFrameBufferAndTexture(){
        glGenFramebuffers(frameBuffer.length, frameBuffer, 0);

        glGenTextures(frameTexture.length, frameTexture, 0);
        glBindTexture(GL_TEXTURE_2D, frameTexture[0]);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,GL_RGBA,GL_UNSIGNED_BYTE,null);
        setTextureParameters();
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void setTextureParameters(){
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
    }

    public void bindFrameBufferAndTexture(){
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer[0]);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D, frameTexture[0],0);
    }

    public void unBindFrameBuffer(){
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
}
