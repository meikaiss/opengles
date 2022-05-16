#version 300 es
in vec4 vPosition;
in vec2 aCoordinate;
uniform mat4 u_Matrix;
out vec4 vColor;
out vec2 vCoordinate;
void main(){
    gl_Position=u_Matrix*vPosition;
    float color;
    if (vPosition.z>0.0){
        color=vPosition.z;
    } else {
        color=-vPosition.z;
    }
    vColor=vec4(color, color, color, 1.0);
    vCoordinate=aCoordinate;
}