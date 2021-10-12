precision mediump float;
//外部传入的源纹理
uniform sampler2D uSampler;
//裁剪形状纹理
uniform sampler2D uSampler2;
varying vec2 vCoordinate;
void main(){
    vec4 sourceColor = texture2D(uSampler, vCoordinate);
    vec4 sourceColor2 = texture2D(uSampler2, vCoordinate);
    if (sourceColor2.a==0.0){
        //若裁剪形状纹理的像素是全透明，则此像素不需要显示高斯模糊色
        gl_FragColor = vec4(0, 0, 0, 0);
    } else {
        gl_FragColor = sourceColor+sourceColor2;
    }
}