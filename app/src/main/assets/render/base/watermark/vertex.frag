uniform mat4 uMatrix;
attribute vec4 aPos;
attribute vec2 aCoordinate;
varying vec2 vCoordinate;
void main(){
    vCoordinate = aCoordinate;
    gl_Position = uMatrix * aPos;
}