package com.demo.demos.filter;

import static android.opengl.GLES30.*;
/**
 * Created by wangyt on 2019/5/24
 */
public class FBOOesFilter extends OesFilter {
    public int[] frameBuffer = new int[1];
    public int[] frameTexture = new int[1];

    public FBOOesFilter() {
        super();
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
