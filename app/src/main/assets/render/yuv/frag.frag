varying vec2 v_texCoord;
uniform sampler2D y_texture;
uniform sampler2D uv_texture;

void main (void){
    float r, g, b, y, u, v;
    y = texture2D(y_texture, v_texCoord).r; //Y代表亮度，数值越高，图像越亮。可增加一定数值，例如+0.1用于图像美白效果
    u = texture2D(uv_texture, v_texCoord).a - 0.5;
    v = texture2D(uv_texture, v_texCoord).r - 0.5;

    //值域区间为[0,255]的YUV转换成RGB的计算公式，V-128对应到纹理中[0,1.0]的区间即为v-0.5，上面取出u和v的值时为方便后续计算，直接提前减0.5
    //R = Y + 1.4075 *（V-128）
    //G = Y – 0.3455 *（U –128） – 0.7169 *（V –128）
    //B = Y + 1.779 *（U – 128

    //测试代码，用于判断shader相关代码是否生效
    //u = 0.1;
    //v = 0.1;

    r = y + 1.13983*v;
    g = y - 0.39465*u - 0.58060*v;
    b = y + 2.03211*u;

    gl_FragColor = vec4(r, g, b, 1.0);
}
