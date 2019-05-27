package com.demo.demos.filter;

import com.demo.demos.R;
import com.demo.demos.utils.GLUtil;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static com.demo.demos.utils.GLUtil.UNIFORM_COLOR_FLAG;

/**
 * Created by wangyt on 2019/5/27
 */
public class ColorFilter extends BaseFilter {
    public static int COLOR_FLAG = 0;

    public int hColorFlag;

    @Override
    public int initProgram() {
        return GLUtil.createAndLinkProgram(R.raw.texture_vertex_shader, R.raw.texture_color_fragtment_shader);
    }

    @Override
    public void initAttribLocations() {
        super.initAttribLocations();

        hColorFlag = glGetUniformLocation(program, UNIFORM_COLOR_FLAG);
    }

    @Override
    public void setExtend() {
        glUniform1i(hColorFlag, COLOR_FLAG);
    }
}
