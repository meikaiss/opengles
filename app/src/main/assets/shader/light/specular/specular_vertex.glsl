uniform mat4 uMatrix;
uniform mat4 uModelMatrix;
uniform vec3 uLightColor;
uniform vec3 uLightPos;
uniform vec3 uViewPosLoc;
uniform float uDiffuseStrength;
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

    //镜面反射
    float specularStrength = 0.9;
    //视角和顶点的单位向量
    vec3 viewDir = normalize(uViewPosLoc - fragPos);
    //调用opengl-shader内置函数reflect，计算光的反射向量
    vec3 reflectDir = reflect(-lightDir, unitNormal);
    float spec = pow(max(dot(unitNormal, reflectDir), 0.0), 16.0);
    specular = specularStrength * spec * uLightColor;

    vColor = aColor;
}
