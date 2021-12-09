uniform mat4 uMatrix;
uniform mat4 uModelMatrix;
uniform vec3 uLightColor;
uniform vec3 uLightPos;
uniform vec3 uViewPosLoc;
uniform float uSpecularStrength;//镜面反射强度
attribute vec4 aPosition;
attribute vec3 aNormal;
attribute vec4 aColor;
varying vec3 specular;
varying vec4 vColor;

void main() {
    gl_Position = uMatrix * aPosition;

    vec3 fragPos = vec3(uModelMatrix * aPosition);
    //fragPos = vec3(gl_Position);

    //顶点的单位法线
    vec3 unitNormal = normalize(vec3(uModelMatrix * vec4(aNormal, 1.0)));
    //unitNormal = normalize(vec3(uMatrix * vec4(aNormal, 1.0)));

    //从顶点到光源的单位向量
    vec3 lightDir = normalize(uLightPos - fragPos);

    //观察点和顶点的单位向量
    vec3 viewDir = normalize(uViewPosLoc - fragPos);
    //调用opengl-shader内置函数reflect，计算光的反射向量
    vec3 reflectDir = reflect(-lightDir, unitNormal);
    /**
        dot为opengl内置的点积函数
        对于向量a和向量b，a和b的点积公式为：将对应分量逐个相乘，然后再把所得积相加得到的数
        要求：向量a和向量b都是一维向量，而且行列数都相同

        一个物体的反光度越高，反射光的能力越强，散射得越少，高光点就会越小。
    */
    float shininess = 32.0;
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), shininess);

    specular = uSpecularStrength * spec * uLightColor;

    vColor = aColor;
}
