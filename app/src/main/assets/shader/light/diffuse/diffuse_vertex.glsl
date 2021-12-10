uniform mat4 uMatrix;
uniform mat4 uModelMatrix;
uniform vec3 uLightColor;
uniform vec3 uLightPos;
uniform float uDiffuseStrength;
attribute vec4 aPosition;
attribute vec3 aNormal;
attribute vec4 aColor;
varying vec3 diffuse;
varying vec4 vColor;

void main() {
    //4行4列的矩阵在左，乘以4行1列的向量在右，得到4行1列的向量
    gl_Position = uMatrix * uModelMatrix * aPosition;

    vec3 fragPos = vec3(uModelMatrix * aPosition);

    //顶点的单位法线，normalize是opengl内置的函数，用于向量标准化，标准化后的向量称为单位向量，其长度=1
    vec3 unitNormal = normalize(vec3(uModelMatrix * vec4(aNormal, 1.0)));

    //从顶点到光源的单位向量
    vec3 lightDir = normalize(uLightPos - fragPos);

    //上面的两个向量进行点乘，点乘是通过将对应分量逐个相乘，然后再把所得积相加来计算的
    float diff = max(dot(unitNormal, lightDir), 0.0);
    diffuse = uDiffuseStrength * diff * uLightColor;

    vColor = aColor;
}