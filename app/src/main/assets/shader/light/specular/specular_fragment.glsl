precision mediump float;

varying vec4 vColor;
varying vec3 specular;

void main() {
    gl_FragColor = vec4(specular * vec3(vColor), vColor.a);
}