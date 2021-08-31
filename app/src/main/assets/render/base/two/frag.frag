precision mediump float;
uniform sampler2D uSampler;
uniform sampler2D uSampler2;
varying vec2 vCoordinate;
void main(){
    vec4 sourceColor = texture2D(uSampler, vCoordinate);
    vec4 sourceColor2 = texture2D(uSampler2, vCoordinate);
    if(sourceColor2.a==0.0){
        gl_FragColor = sourceColor2;
    }else{
        gl_FragColor = sourceColor+sourceColor2;
    }
}