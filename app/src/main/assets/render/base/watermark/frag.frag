precision mediump float;
uniform sampler2D uSampler;
uniform sampler2D uSampler2;
varying vec2 vCoordinate;
void main(){
    vec4 color1 = texture2D(uSampler, vCoordinate);
    vec4 color2 = texture2D(uSampler2, vCoordinate);
    gl_FragColor = color1 + color2;
}