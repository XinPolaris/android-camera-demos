#version 300 es
precision mediump float;

in vec2 v_texCoord;
out vec4 outColor;
uniform sampler2D s_texture;

uniform int colorFlag;//滤镜类型

void main(){
    vec4 tmpColor = texture(s_texture, v_texCoord);

    if(colorFlag == 1){//灰度
        float weightMean = tmpColor.r * 0.3 + tmpColor.g * 0.59 + tmpColor.b * 0.11;
        tmpColor.r = tmpColor.g = tmpColor.b = weightMean;
    }else if(colorFlag == 2){//黑白
        float threshold = 0.5;
        float mean = (tmpColor.r + tmpColor.g + tmpColor.b) / 3.0;
        tmpColor.r = tmpColor.g = tmpColor.b = mean >= threshold ? 1.0 : 0.0;
    }else if(colorFlag == 3){//反向
        tmpColor.r = 1.0 - tmpColor.r;
        tmpColor.g = 1.0 - tmpColor.g;
        tmpColor.b = 1.0 - tmpColor.b;
    }

    outColor = tmpColor;
}

//将颜色值约束在[0.0-1.0] 之间
void checkColor(vec4 color){
    color.r=max(min(color.r,1.0),0.0);
    color.g=max(min(color.g,1.0),0.0);
    color.b=max(min(color.b,1.0),0.0);
    color.a=max(min(color.a,1.0),0.0);
}