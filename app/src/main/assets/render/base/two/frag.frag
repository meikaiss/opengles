precision mediump float;
//外部传入的源纹理
uniform sampler2D uSampler;
//外部传入的第二个源纹理
uniform sampler2D uSampler2;
//裁剪形状纹理，裁剪形状外的区域填充第一个纹理，形状内的区域填充第二个纹理与裁剪纹理的叠加
uniform sampler2D uSamplerDrawable;
varying vec2 vCoordinate;
void main(){
    vec4 sourceColor = texture2D(uSampler, vCoordinate);
    vec4 sourceColor2 = texture2D(uSampler2, vCoordinate);
    vec4 sourceColorDrawable = texture2D(uSamplerDrawable, vCoordinate);
    if(sourceColorDrawable.a==0.0){
        gl_FragColor = sourceColor;
    }else{
        gl_FragColor = sourceColor2;
    }
}