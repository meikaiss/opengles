precision mediump float;

varying vec4 vColor;
varying vec3 specular;

void main() {
    vec3 finalColor = specular * vec3(vColor);
    gl_FragColor = min(vec4(finalColor, vColor.a), vec4(1.0));
}