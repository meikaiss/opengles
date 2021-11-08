attribute vec4 aPos;
attribute vec2 aCoordinate;
varying vec2 v_texCoord;
void main(){
    v_texCoord = aCoordinate;
    gl_Position = aPos;
}