#version 300 es

/**
    version 300 es 声明为指定使用 OpenGL ES 3.0 版本
    不添加版本声明或者使用 #version 100 es 声明版本则指定使用 OpenGL ES 2.0。
    备注：在opengles 2.0 时才诞生可编程的图形管线，所以版本声明为 #version 100 es ，后来为了使版本号相匹配，OpenGL ES 3.0 的 shader 版本直接从1.0 跳到了 3.0 。
*/

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec4 aColor;

out vec4 vColor;

void main() {
    gl_Position  = aPosition;
    vColor = aColor;
}