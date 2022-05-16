#version 300 es
precision mediump float;
in vec4 vColor;
in vec2 vCoordinate;
uniform sampler2D uTexture;
out vec4 fragColor;
void main() {
    vec4 color = texture(uTexture, vCoordinate);
//    fragColor = vColor;
    fragColor = color;
}