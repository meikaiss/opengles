precision mediump float;
uniform sampler2D uSampler;
varying vec2 vCoordinate;
void main(){
    vec4 color1 = texture2D(uSampler, vCoordinate);
    gl_FragColor = color1;
}