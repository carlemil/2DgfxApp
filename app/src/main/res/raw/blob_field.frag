#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

float blob(vec2 uv, vec2 c, float r) {
    float d = length(uv - c);
    return r * r / (d * d + 0.001);
}

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;
    float t = u_time * 0.6;

    float field = 0.0;
    for (int i = 0; i < 6; i++) {
        float fi = float(i);
        vec2 pos = vec2(
            0.5 + 0.38 * cos(t * (0.9 + fi * 0.17) + fi * 1.05),
            0.5 + 0.38 * sin(t * (1.1 + fi * 0.13) + fi * 0.73)
        );
        field += blob(uv, pos, 0.08 + 0.03 * sin(t + fi));
    }

    // Touch attracts toward finger
    field += blob(uv, u_touch, 0.12);

    float iso = smoothstep(0.7, 1.3, field);

    // Warm orange-pink palette
    vec3 col = mix(
        vec3(0.05, 0.02, 0.1),
        vec3(1.0, 0.5, 0.1) + vec3(0.0, 0.3, 0.5) * sin(field * 3.0 + t),
        iso
    );

    fragColor = vec4(col, 1.0);
}
