uniform mat4 uMatrix;
uniform mat4 uModelMatrix;
uniform vec3 uLightColor;
uniform vec3 uLightPos;
uniform vec3 uViewPosLoc;
uniform float uSpecularStrength; //镜面反射强度
attribute vec4 aPosition;
attribute vec3 aNormal;
attribute vec4 aColor;
varying vec3 specular;
varying vec4 vColor;

void main() {
    gl_Position = uMatrix * aPosition;

    vec3 fragPos = vec3(uModelMatrix * aPosition);

    //顶点的单位法线
    vec3 unitNormal = normalize(vec3(uModelMatrix * vec4(aNormal, 1.0)));
    //从顶点到光源的单位向量
    vec3 lightDir = normalize(uLightPos - fragPos);


    //观察点和顶点的单位向量
    vec3 viewDir = normalize(uViewPosLoc - fragPos);
    //调用opengl-shader内置函数reflect，计算光的反射向量
    vec3 reflectDir = reflect(-lightDir, unitNormal);
    /**
    两个矩阵的行数和列数必须相等才能进行点乘
    两个矩阵相乘，结果为两个矩阵对应元素相乘组成的矩阵，即
    */
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 16.0);

    specular = uSpecularStrength * spec * uLightColor;

    vColor = aColor;
}
