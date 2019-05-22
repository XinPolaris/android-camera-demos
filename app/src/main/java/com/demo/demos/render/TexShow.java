package com.demo.demos.render;

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
import static com.demo.demos.utils.GLUtil.textureCoordOes;
import static com.demo.demos.utils.GLUtil.vertex;

/**
 * Created by wangyt on 2019/5/22
 */
public class TexShow {
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureCoordBuffer;

    private int[] textureId;
    private int program;
    private int hVertex, hTextureCoord, hTexture;

    private int width, height;

    public void setTextureId(int[] textureId){
        this.textureId = textureId;
    }

    public TexShow() {
        initBuffer();
    }

    private void initBuffer(){
        vertexBuffer = GLUtil.getFloatBuffer(vertex);
        textureCoordBuffer = GLUtil.getFloatBuffer(textureCoord);
    }

    public void onSurfaceCreated(){
        createTexture();
        createProgram();
        getAttribLocations();
    }

    public void onSurfaceChanged(int width, int height){
        this.width = width;
        this.height = height;
    }

    public void onDraw(){
        draw();
    }

    private void createTexture(){
        glGenTextures(textureId.length, textureId, 0);
    }

    private void createProgram(){
        program = GLUtil.createAndLinkProgram(R.raw.texture_vertex_shader, R.raw.texture_fragtment_shader);
    }

    private void getAttribLocations(){
        hVertex = glGetAttribLocation(program, VERTEX_ATTRIB_POSITION);
        hTextureCoord = glGetAttribLocation(program, VERTEX_ATTRIB_TEXTURE_POSITION);
        hTexture = glGetUniformLocation(program, UNIFORM_TEXTURE);
    }

    private void draw(){
        glViewport(0,0,width,height);

        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(program);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId[0]);
        glUniform1i(hTexture, 0);

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
        glDrawArrays(GL_TRIANGLE_FAN,0,vertex.length / 3);
        glDisableVertexAttribArray(hVertex);
        glDisableVertexAttribArray(hTextureCoord);
    }
}
