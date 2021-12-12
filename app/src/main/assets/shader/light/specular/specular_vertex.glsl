uniform mat4 uProjectMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uModelMatrix;
uniform mat4 uModelViewInvertTransposeMatrix;//模型矩阵 * 视图矩阵 的 逆矩阵 的 转置矩阵

uniform vec3 uLightColor;
uniform vec3 uLightPos;
uniform float uSpecularStrength;//镜面反射强度

uniform vec3 uViewPosLoc;

attribute vec4 aPosition;
attribute vec4 aNormal;
attribute vec4 aColor;

varying vec3 specular;
varying vec4 vColor;

// 边界值处理
vec3 clampCoordinate(vec3 coordinate) {
    return vec3(clamp(coordinate.x, 0.0, 1.0), clamp(coordinate.y, 0.0, 1.0), clamp(coordinate.z, 0.0, 1.0));
}

void main() {
    gl_Position = uProjectMatrix * uViewMatrix * uModelMatrix * aPosition;

    vec3 modelPos = vec3(aPosition);

    //顶点的单位法线
    vec3 unitNormal = normalize(vec3(aNormal));

    //从顶点到光源的单位向量
    vec3 lightDir = normalize(uLightPos - modelPos);

    //观察点和顶点的单位向量
    vec3 viewDir = normalize(uViewPosLoc - modelPos);
    //调用opengl-shader内置函数reflect，计算光的反射向量
    vec3 reflectDir = reflect(-lightDir, unitNormal);
    /**
        dot为opengl内置的点积函数
        对于向量a和向量b，a和b的点积公式为：将对应分量逐个相乘，然后再把所得积相加得到的数
        要求：向量a和向量b都是一维向量，而且行列数都相同

        一个物体的反光度越高，反射光的能力越强，散射得越少，高光点就会越小。
    */
    float shininess = 64.0;
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), shininess);

    specular = uSpecularStrength * spec * uLightColor;

    specular = clampCoordinate(specular);

    vColor = aColor;
}
