precision mediump float;
varying vec4 vColor;
varying vec3 ambient;

void main() {
    //*表示的是各个分量相乘，就能得到环境光与物体材质颜色混合的效果
    //冯氏光照模型的原理理解参考 https://zhuanlan.zhihu.com/p/427477685
    vec3 finalColor = ambient * vec3(vColor);
    gl_FragColor = min(vec4(finalColor, vColor.a), vec4(1.0));
}