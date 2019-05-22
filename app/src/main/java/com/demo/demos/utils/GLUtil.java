package com.demo.demos.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES30.*;
import static android.opengl.GLUtils.texImage2D;

/**
 * Created by wangyt on 2019/5/9
 */
public class GLUtil {
    private static final String TAG = "opengl-demos";
    //float 字节数
    public static final int BYTES_PER_FLOAT = 4;

    private static Context context;

    public static void init(Context ctx){
        context = ctx;
    }

    /*********************** 着色器、程序 ************************/
    public static String loadShaderSource(int resId){
        StringBuilder res = new StringBuilder();

        InputStream is = context.getResources().openRawResource(resId);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String nextLine;
            try {
                while ((nextLine = br.readLine()) != null) {
                    res.append(nextLine);
                    res.append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        return res.toString();
    }

    /**
     * 加载着色器源，并编译
     *
     * @param type         顶点着色器（GL_VERTEX_SHADER）/片段着色器（GL_FRAGMENT_SHADER）
     * @param shaderSource 着色器源
     * @return 着色器
     */
    public static int loadShader(int type, String shaderSource){
        //创建着色器对象
        int shader = glCreateShader(type);
        if (shader == 0) return 0;//创建失败
        //加载着色器源
        glShaderSource(shader, shaderSource);
        //编译着色器
        glCompileShader(shader);
        //检查编译状态
        int[] compiled = new int[1];
        glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, glGetShaderInfoLog(shader));
            glDeleteShader(shader);
            return 0;//编译失败
        }

        return shader;
    }

    public static int createAndLinkProgram(int vertextShaderResId, int fragmentShaderResId){
        //获取顶点着色器
        int vertexShader = GLUtil.loadShader(GL_VERTEX_SHADER, loadShaderSource(vertextShaderResId));
        if (0 == vertexShader){
            Log.e(TAG, "failed to load vertexShader");
            return 0;
        }
        //获取片段着色器
        int fragmentShader = GLUtil.loadShader(GL_FRAGMENT_SHADER, loadShaderSource(fragmentShaderResId));
        if (0 == fragmentShader){
            Log.e(TAG, "failed to load fragmentShader");
            return 0;
        }
        int program = glCreateProgram();
        if (program == 0){
            Log.e(TAG, "failed to create program");
        }
        //绑定着色器到程序
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        //连接程序
        glLinkProgram(program);
        //检查连接状态
        int[] linked = new int[1];
        glGetProgramiv(program,GL_LINK_STATUS, linked, 0);
        if (linked[0] == 0){
            glDeleteProgram(program);
            Log.e(TAG, "failed to link program");
            return 0;
        }
        return program;
    }

    /*********************** （暂时放这，后面统一组织）**************/
    public static FloatBuffer getFloatBuffer(float[] array){
        //将顶点数据拷贝映射到 native 内存中，以便opengl能够访问
        FloatBuffer buffer = ByteBuffer
                .allocateDirect(array.length * BYTES_PER_FLOAT)//直接分配 native 内存，不会被gc
                .order(ByteOrder.nativeOrder())//和本地平台保持一致的字节序（大/小头）
                .asFloatBuffer();//将底层字节映射到FloatBuffer实例，方便使用
        buffer
                .put(array)//将顶点拷贝到 native 内存中
                .position(0);//每次 put position 都会 + 1，需要在绘制前重置为0

        return buffer;
    }

    public static final String VERTEX_ATTRIB_POSITION = "a_Position";
    public static final int VERTEX_ATTRIB_POSITION_SIZE = 3;
    public static final String VERTEX_ATTRIB_TEXTURE_POSITION = "a_texCoord";
    public static final int VERTEX_ATTRIB_TEXTURE_POSITION_SIZE = 2;
    public static final String UNIFORM_TEXTURE = "s_texture";

    public static final float[] vertex ={
            -1f,1f,0.0f,//左上
            -1f,-1f,0.0f,//左下
            1f,-1f,0.0f,//右下
            1f,1f,0.0f//右上
    };

    // 纹理坐标，（s,t）
    // 使用后置相机，旋转90度
    // t坐标方向和顶点y坐标反着
    public static final float[] textureCoordOes = {
            0.0f,1.0f,
            1.0f,1.0f,
            1.0f,0.0f,
            0.0f,0.0f
    };
    //
    public static final float[] textureCoord = {
            0.0f,1.0f,
            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f
    };
}
