precision mediump float;
varying vec4 vColor;
varying vec3 ambient;

void main() {
    /**
        矩阵的乘法类型：
        一、矩阵的点乘
          a、两个矩阵的行数和列数必须相等才能进行点乘
          b、两个矩阵相乘，结果为两个矩阵对应元素相乘组成的矩阵，即
        二、矩阵的叉乘
          a、两个矩阵叉乘必须满足第一个矩阵的列数与第二个矩阵的行数相等


        *星花符号应用于不同对象时，有不同含义
          1、矩阵 * 列向量：表示按矩阵叉乘
          2、列向量 * 列向量：表示对应元素相乘，得到的新向量。要求向量的行列数相同
    */

    /**
        *表示的是各个分量相乘，就能得到环境光与物体材质颜色混合的效果
        点乘用内置函数dot，叉乘用内置函数cross，分量相乘用*
        冯氏光照模型的原理理解参考 https://zhuanlan.zhihu.com/p/427477685
    */
    vec3 finalColor = ambient * vec3(vColor);
    gl_FragColor = min(vec4(finalColor, vColor.a), vec4(1.0));
}