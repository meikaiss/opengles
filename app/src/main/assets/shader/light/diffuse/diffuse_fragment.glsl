precision mediump float;

varying vec4 vColor;
varying vec3 diffuse;

void main() {
    vec3 finalColor = diffuse * vec3(vColor);
    gl_FragColor = min(vec4(finalColor, vColor.a), vec4(1.0));
}