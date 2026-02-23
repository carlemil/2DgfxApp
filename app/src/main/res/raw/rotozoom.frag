#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

void main() {
    vec2 uv = (gl_FragCoord.xy - 0.5 * u_resolution) / u_resolution.y;

    // Touch x = rotation speed, touch y = zoom
    float rotSpeed = (u_touch.x - 0.5) * 4.0;
    float zoom     = 1.0 + u_touch.y * 4.0;

    float angle = u_time * (0.4 + rotSpeed);
    float cosA  = cos(angle) * zoom;
    float sinA  = sin(angle) * zoom;

    // Rotate + scale
    vec2 tex = vec2(cosA * uv.x - sinA * uv.y,
                    sinA * uv.x + cosA * uv.y);

    // Tiled checkerboard with coloured stripes
    float cx = step(0.5, fract(tex.x));
    float cy = step(0.5, fract(tex.y));
    float check = abs(cx - cy);

    float hue = fract(floor(tex.x) * 0.137 + floor(tex.y) * 0.241 + u_time * 0.06);
    vec3 colA  = 0.5 + 0.5 * cos(6.2832 * (vec3(0.0,0.33,0.67) + hue));
    vec3 colB  = 0.5 + 0.5 * cos(6.2832 * (vec3(0.5,0.83,0.17) + hue));
    vec3 col   = mix(colA, colB, check);

    fragColor = vec4(col, 1.0);
}
