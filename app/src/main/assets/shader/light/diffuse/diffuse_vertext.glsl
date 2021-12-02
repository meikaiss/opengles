uniform mat4 uMatrix;
uniform mat4 uModelMatrix;
uniform vec3 uLightColor;
uniform vec3 uLightPos;
attribute vec4 aPosition;
attribute vec3 aNormal;
attribute vec4 aColor;
varying vec3 diffuse;
varying vec4 vColor;

void main() {
    gl_Position = uMatrix*aPosition;

    vec3 fragPos = vec3(uModelMatrix * aPosition);

    //漫反射强度
    float diffuseStrength = 0.8;
    //顶点的单位法线
    vec3 unitNormal = normalize(vec3(uModelMatrix * vec4(aNormal, 1.0)));
    //从顶点到光源的单位向量
    vec3 lightDir = normalize(uLightPos - fragPos);
    //上面的两个向量进行点乘
    float diff = max(dot(unitNormal, lightDir), 0.0);
    diffuse = diffuseStrength * diff * uLightColor;

    vColor = aColor;
}