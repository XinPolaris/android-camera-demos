package com.demo.demos.render;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.demo.demos.R;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static android.opengl.EGL14.EGL_ALPHA_SIZE;
import static android.opengl.EGL14.EGL_BLUE_SIZE;
import static android.opengl.EGL14.EGL_BUFFER_SIZE;
import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;
import static android.opengl.EGL14.EGL_DEFAULT_DISPLAY;
import static android.opengl.EGL14.EGL_GREEN_SIZE;
import static android.opengl.EGL14.EGL_NONE;
import static android.opengl.EGL14.EGL_NO_CONTEXT;
import static android.opengl.EGL14.EGL_NO_DISPLAY;
import static android.opengl.EGL14.EGL_OPENGL_ES2_BIT;
import static android.opengl.EGL14.EGL_RED_SIZE;
import static android.opengl.EGL14.EGL_RENDERABLE_TYPE;
import static android.opengl.EGL14.EGL_SURFACE_TYPE;
import static android.opengl.EGL14.EGL_WINDOW_BIT;
import static android.opengl.EGL14.eglChooseConfig;
import static android.opengl.EGL14.eglCreateContext;
import static android.opengl.EGL14.eglCreateWindowSurface;
import static android.opengl.EGL14.eglDestroyContext;
import static android.opengl.EGL14.eglDestroySurface;
import static android.opengl.EGL14.eglGetDisplay;
import static android.opengl.EGL14.eglGetError;
import static android.opengl.EGL14.eglInitialize;
import static android.opengl.EGL14.eglMakeCurrent;
import static android.opengl.EGL14.eglSwapBuffers;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static com.demo.demos.utils.EGLUtil.createAndLinkProgram;
import static com.demo.demos.utils.EGLUtil.getTextureCoordBuffer;
import static com.demo.demos.utils.EGLUtil.getVertextBuffer;
import static com.demo.demos.utils.EGLUtil.loadShader;
import static com.demo.demos.utils.EGLUtil.loadShaderSource;
import static com.demo.demos.utils.EGLUtil.loadTexture;

/**
 * Created by wangyt on 2019/5/10
 */
public class TextureRender extends HandlerThread {

    private SurfaceTexture surfaceTexture;

    private EGLConfig eglConfig;
    private EGLDisplay eglDisplay;
    private EGLContext eglContext;
    private EGLSurface eglSurface;

    public TextureRender() {
        super("texturerender");
    }

    public void initEGL(SurfaceTexture stexture){
        surfaceTexture = stexture;

        //获取显示设备
        eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL_NO_DISPLAY){
            throw new RuntimeException("egl error:" + eglGetError());
        }
        //初始化EGL
        int[] version = new int[2];
        if (!eglInitialize(eglDisplay, version,0,version,1)){
            throw new RuntimeException("egl error:" + eglGetError());
        }
        //EGL选择配置
        int[] configAttribList = {
                EGL_BUFFER_SIZE, 32,
                EGL_ALPHA_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_RED_SIZE, 8,
                EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
                EGL_NONE
        };
        int[] numConfig = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        if(!eglChooseConfig(eglDisplay,
                configAttribList, 0,
                configs,0, configs.length,
                numConfig,0)){
            throw new RuntimeException("egl error:" + eglGetError());
        }
        eglConfig = configs[0];
        //创建ELG上下文
        int[] contextAttribList = {
                EGL_CONTEXT_CLIENT_VERSION,2,
                EGL_NONE
        };
        eglContext = eglCreateContext(eglDisplay, eglConfig,EGL_NO_CONTEXT,contextAttribList,0);
        if (eglContext == EGL_NO_CONTEXT){
            throw new RuntimeException("egl error:" + eglGetError());
        }
        //创建屏幕上渲染区域：EGL窗口
        int[] surfaceAttribList = {EGL_NONE};
        eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, surfaceTexture, surfaceAttribList, 0);
        Log.d("GLFilterFragment", "createEGL: ");
    }

    private void destroyEGL(){
        eglDestroyContext(eglDisplay, eglContext);
        eglContext = EGL_NO_CONTEXT;
        eglDisplay = EGL_NO_DISPLAY;
    }

    public void release(){
        new Handler(getLooper()).post(new Runnable() {
            @Override
            public void run() {
                destroyEGL();
                quit();
            }
        });
    }

    public void render(ImageReader imageReader, int width, int height){
        Log.d("GLFilterFragment", "render: ");

        Image image = imageReader.acquireLatestImage();
        if (image == null) {
            return;
        }
        final ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        if (surfaceTexture == null){
            image.close();
            return;
        }

        render(bitmap, width, height);
        image.close();
    }

    private void render(Bitmap bitmap, int width, int height){
        //指定当前上下文
        eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
        //获取着色器
        int texVertexShader = loadShader(GL_VERTEX_SHADER, loadShaderSource(R.raw.texture_vertex_shader));
        int texFragmentShader = loadShader(GL_FRAGMENT_SHADER, loadShaderSource(R.raw.texture_fragtment_shader));
        //创建并连接程序
        int program = createAndLinkProgram(texVertexShader, texFragmentShader);
        //设置清除渲染时的颜色
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        //设置视口
        glViewport(0, 0, width, height);
        //获取顶点、纹理坐标数据
        FloatBuffer vertexBuffer = getVertextBuffer();
        FloatBuffer texCoordBuffer = getTextureCoordBuffer();
        //擦除屏幕
        glClear(GL_COLOR_BUFFER_BIT);
        //使用程序
        glUseProgram(program);

        //绑定顶点、纹理坐标到指定属性位置
        int aPosition = glGetAttribLocation(program, "a_Position");
        int aTexCoord = glGetAttribLocation(program, "a_texCoord");
        glVertexAttribPointer(aPosition,3,GL_FLOAT,false,0,vertexBuffer);
        glVertexAttribPointer(aTexCoord, 2, GL_FLOAT, false, 0, texCoordBuffer);
        glEnableVertexAttribArray(aPosition);
        glEnableVertexAttribArray(aTexCoord);
        //绑定纹理
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, loadTexture(bitmap));
        //Set the sampler texture unit to 0
        glUniform1i(glGetUniformLocation(program, "s_texture"),0);
        //绘制
        glDrawArrays(GL_TRIANGLES,0,3);
        //交换 surface 和显示器缓存
        eglSwapBuffers(eglDisplay, eglSurface);
    }
}
