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
    private static final String VERTEX_ATTRIB_POSITION = "a_Position";
    private static final int VERTEX_ATTRIB_POSITION_SIZE = 3;
    private static final String VERTEX_ATTRIB_TEXTURE_POSITION = "a_texCoord";
    private static final int VERTEX_ATTRIB_TEXTURE_POSITION_SIZE = 2;
    private static final String UNIFORM_TEXTURE = "s_texture";

    private  float[] vertex ={
            -1f,1f,0.0f,//左上
            -1f,-1f,0.0f,//左下
            1f,-1f,0.0f,//右下
            1f,1f,0.0f//右上
    };

    //纹理坐标，（s,t），t坐标方向和顶点y坐标反着
    public float[] textureCoord = {
            0.0f,1.0f,
            1.0f,1.0f,
            1.0f,0.0f,
            0.0f,0.0f
    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureCoordBuffer;

    private int program;

    private int[] textureId = new int[1];
    public SurfaceTexture surfaceTexture;

    public GLRender() {
        //初始化顶点数据
        initVertexAttrib();
    }

    private void initVertexAttrib() {
        textureCoordBuffer = GLUtil.getFloatBuffer(textureCoord);
        vertexBuffer = GLUtil.getFloatBuffer(vertex);
    }

    public SurfaceTexture getSurfaceTexture(){
        return surfaceTexture;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //创建纹理对象
        glGenTextures(textureId.length, textureId, 0);
        //将纹理对象绑定到srufaceTexture
        surfaceTexture = new SurfaceTexture(textureId[0]);
        //创建并连接程序
        program = GLUtil.createAndLinkProgram(R.raw.texture_vertex_shader, R.raw.texture_fragtment_shader);
        //设置清除渲染时的颜色
        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //srufaceTexture 获取新的纹理数据
        surfaceTexture.updateTexImage();
        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(program);

        int vertexLoc = glGetAttribLocation(program, VERTEX_ATTRIB_POSITION);
        int textureLoc = glGetAttribLocation(program, VERTEX_ATTRIB_TEXTURE_POSITION);

        glEnableVertexAttribArray(vertexLoc);
        glEnableVertexAttribArray(textureLoc);

        glVertexAttribPointer(vertexLoc,
                VERTEX_ATTRIB_POSITION_SIZE,
                GL_FLOAT,
                false,
                0,
                vertexBuffer);

        glVertexAttribPointer(textureLoc,
                VERTEX_ATTRIB_TEXTURE_POSITION_SIZE,
                GL_FLOAT,
                false,
                0,
                textureCoordBuffer);

        //绑定0号纹理单元纹理
        glActiveTexture(GL_TEXTURE0);
        //将纹理放到当前单元的 GL_TEXTURE_BINDING_EXTERNAL_OES 目标对象中
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId[0]);
        //设置纹理过滤参数
        glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER,GL_NEAREST);
        glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
        //将片段着色器的纹理属性值（s_texture）设置为 0 号单元
        int uTextureLoc = glGetUniformLocation(program, UNIFORM_TEXTURE);
        glUniform1i(uTextureLoc,0);

        glDrawArrays(GL_TRIANGLE_FAN,0,vertex.length / 3);

        glDisableVertexAttribArray(vertexLoc);
        glDisableVertexAttribArray(textureLoc);
    }
}
