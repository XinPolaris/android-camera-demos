package com.demo.demos.filter;

import android.util.Log;

import com.demo.demos.R;
import com.demo.demos.utils.GLUtil;

import java.nio.FloatBuffer;

import static android.opengl.GLES30.*;
import static com.demo.demos.utils.GLUtil.UNIFORM_TEXTURE;
import static com.demo.demos.utils.GLUtil.VERTEX_ATTRIB_POSITION;
import static com.demo.demos.utils.GLUtil.VERTEX_ATTRIB_POSITION_SIZE;
import static com.demo.demos.utils.GLUtil.VERTEX_ATTRIB_TEXTURE_POSITION;
import static com.demo.demos.utils.GLUtil.VERTEX_ATTRIB_TEXTURE_POSITION_SIZE;
import static com.demo.demos.utils.GLUtil.textureCoord;
import static com.demo.demos.utils.GLUtil.vertex;

/**
 * Created by wangyt on 2019/5/24
 */
public class BaseFilter {
    public FloatBuffer vertexBuffer;
    public FloatBuffer textureCoordBuffer;

    public int[] textureId;
    public int program;
    public int hVertex, hTextureCoord, hTexture;

    public int width, height;

    public BaseFilter() {
        initBuffer();
    }

    public void initBuffer(){
        vertexBuffer = GLUtil.getFloatBuffer(vertex);
        textureCoordBuffer = GLUtil.getFloatBuffer(textureCoord);
    }

    public int[] getTextureId() {
        return textureId;
    }

    public void setTextureId(int[] textureId) {
        this.textureId = textureId;
    }

    public int[] getOutputTextureId(){
        return null;
    }

    public void onSurfaceCreated(){
        program = initProgram();
        initAttribLocations();
    }

    public void onSurfaceChanged(int width, int height){
        this.width = width;
        this.height = height;
    }

    public void onDraw(){
        setViewPort();
        clear();
        useProgram();
        bindTexture();
        enableVertexAttribs();
        draw();
        disableVertexAttribs();
    }

    public int initProgram(){
        return GLUtil.createAndLinkProgram(R.raw.texture_vertex_shader, R.raw.texture_fragtment_shader);
    }

    public void initAttribLocations(){
        hVertex = glGetAttribLocation(program, VERTEX_ATTRIB_POSITION);
        hTextureCoord = glGetAttribLocation(program, VERTEX_ATTRIB_TEXTURE_POSITION);
        hTexture = glGetUniformLocation(program, UNIFORM_TEXTURE);
    }

    public void setViewPort(){
        glViewport(0,0,width,height);
    }

    public void clear(){
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void useProgram(){
        glUseProgram(program);
    }

    public void bindTexture(){
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, getTextureId()[0]);
        glUniform1i(hTexture, 0);
    }

    public void enableVertexAttribs(){
        glEnableVertexAttribArray(hVertex);
        glEnableVertexAttribArray(hTextureCoord);
        glVertexAttribPointer(hVertex,
                VERTEX_ATTRIB_POSITION_SIZE,
                GL_FLOAT,
                false,
                0,
                vertexBuffer);

        glVertexAttribPointer(hTextureCoord,
                VERTEX_ATTRIB_TEXTURE_POSITION_SIZE,
                GL_FLOAT,
                false,
                0,
                textureCoordBuffer);
    }

    public void draw(){
        glDrawArrays(GL_TRIANGLE_FAN,0,vertex.length / 3);
    }

    public void disableVertexAttribs(){
        glDisableVertexAttribArray(hVertex);
        glDisableVertexAttribArray(hTextureCoord);
    }
}
