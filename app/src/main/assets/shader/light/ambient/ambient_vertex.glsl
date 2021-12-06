uniform mat4 uMatrix;
uniform vec3 uLightColor; //环境光源的颜色
uniform float uLightStrong; //环境光源的强度
attribute vec4 aPosition;
attribute vec4 aColor;
varying vec4 vColor;
varying vec3 ambient; //环境光的实际生效值

void main() {
    //4行4列的矩阵 左乘 4行1列的向量 = 4行1列的向量
    gl_Position = uMatrix * aPosition;
    vColor = aColor;
    float ambientStrength = uLightStrong; //环境光源的强度。值越大光线越强，对观察效果的颜色影响越重，但亮度的影响越小；值越小光线越弱，对观察效果的影响越轻，但亮度的影响越大。当强度=0时，表示毫无光线，在毫无光线的漆黑房间里是看不见任何物体的。
    ambient = ambientStrength * uLightColor;
}