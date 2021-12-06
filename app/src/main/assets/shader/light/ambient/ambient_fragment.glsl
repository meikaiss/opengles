precision mediump float;
varying vec4 vColor;
varying vec3 ambient;

void main() {
    vec3 finalColor = ambient * vec3(vColor);
    gl_FragColor = min(vec4(finalColor, vColor.a), vec4(1.0));
}