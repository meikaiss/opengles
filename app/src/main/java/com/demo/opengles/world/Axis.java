package com.demo.opengles.world;

public class Axis {

    private final String vertexShaderCode =
            "uniform mat4 vMatrix;" +
                    "attribute vec4 aPosition;" +
                    "attribute vec4 aColor;" +
                    "varying  vec4 vColor;" +
                    "void main() {" +
                    "  gl_Position = vMatrix*aPosition;" +
                    "  vColor=aColor;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
}
