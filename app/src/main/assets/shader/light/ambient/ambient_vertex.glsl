uniform mat4 uMatrix;
uniform vec3 uLightColor;//环境光源的颜色
uniform float uLightStrong;//环境光源的强度
attribute vec4 aPosition;
attribute vec4 aColor;
varying vec4 vColor;
varying vec3 ambient;//环境光的实际生效值

void main() {
    /**
      4行4列的矩阵 左乘 4行1列的向量 = 4行1列的向量
      规则：在opengl中统一规定，向量都是列向量。因此在进行空间变换时，都是矩阵左乘向量
          （因为按矩阵乘法的定义，左侧矩阵的列数一定要等于右侧矩阵的行数，否则无法计算）
          （而微软的DirectX刚好相反，向量都是行向量）
      结论：行向量左乘还是右乘根本就是矩阵乘法规则的限制，行向量只能左乘而列向量只能右乘矩阵，这和三维图形学没有一点儿的关系。因此有人说OpenGL都是右乘、OSG都是左乘从结论上来说是对的，但是这和OpenGL以及OSG本身并没有半点关系，这只是矩阵乘法的定义
    */
    gl_Position = uMatrix * aPosition;
    vColor = aColor;
    float ambientStrength = uLightStrong;//环境光源的强度。值越大光线越强，对观察效果的颜色影响越重，但亮度的影响越小；值越小光线越弱，对观察效果的影响越轻，但亮度的影响越大。当强度=0时，表示毫无光线，在毫无光线的漆黑房间里是看不见任何物体的。
    ambient = ambientStrength * uLightColor;


    /**
        opengl中数据类型按量划分有三种：标量、矢量、矩阵
        1、标量：即单个数字，如float
        2、矢量： 一维数组，vec2、vec3、vec4都是矢量，在opengl中都属于列向量
        3、矩阵：二维数组，mat4

        glsl中数据类型的加减乘除
        1、标量的加减乘除
            按小学生那一套即可
        2、矢量的加减乘除
            矢量对标量，结果为矢量的每个元素对标量进行加减乘除后，产生的新矩阵
            矢量对矢量，必须两个矢量的行数和列数都相同才能进行，结果为两个矩阵对应元素相乘组成的矢量
        3、矩阵的加减乘除
            矩阵对标量，结果为矩阵的每个元素对标量进行加减乘除后，产生的新矩阵
            矩阵对矢量，把矢量简化为列数为1的矩阵
            矩阵对矩阵，加减操作必须是行数列数相同的矩阵，乘法左列数等于右行数，除法变换为乘以右矩阵的逆矩阵（矩阵乘以它的逆矩阵等于单位矩阵）
    */
}