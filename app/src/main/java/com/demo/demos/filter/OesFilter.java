package com.demo.demos.filter;

import android.opengl.GLES11Ext;
import android.util.Log;

import com.demo.demos.R;
import com.demo.demos.utils.GLUtil;

import static android.opengl.GLES30.*;
import static com.demo.demos.utils.GLUtil.textureCoordOes;
import static com.demo.demos.utils.GLUtil.vertex;

/**
 * Created by wangyt on 2019/5/24
 */
public class OesFilter extends BaseFilter{

    public OesFilter() {
        super();
    }

    @Override
    public void initBuffer() {
        vertexBuffer = GLUtil.getFloatBuffer(vertex);
        textureCoordBuffer = GLUtil.getFloatBuffer(textureCoordOes);
    }

    @Override
    public int initProgram() {
        return GLUtil.createAndLinkProgram(R.raw.texture_vertex_shader, R.raw.texture_oes_fragtment_shader);
    }

    @Override
    public void bindTexture() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, getTextureId()[0]);
        glUniform1i(hTexture, 0);
    }
}
