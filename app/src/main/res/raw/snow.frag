#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

float hash(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;
    vec3 col = vec3(0.0, 0.02, 0.06);  // dark blue background

    // Tilt shifts wind direction
    float wind = u_tilt.x * 0.15;

    for (int layer = 0; layer < 4; layer++) {
        float fl    = float(layer);
        float scale = 6.0 + fl * 4.0;
        float speed = 0.06 + fl * 0.05;
        float size  = 0.35 - fl * 0.07;

        vec2 pos = uv * scale;
        pos.y   += u_time * speed;
        pos.x   += sin(pos.y * 0.8 + u_time * 0.3) * 0.2 + wind * (fl + 1.0);

        vec2 cell = floor(pos);
        vec2 frac = fract(pos) - 0.5;

        float h = hash(cell + fl * 7.0);
        vec2 jitter = vec2(h * 2.0 - 1.0, hash(cell + fl * 3.0) * 2.0 - 1.0) * 0.3;
        float d  = length(frac - jitter);
        float flake = smoothstep(size, size * 0.3, d);

        col += vec3(flake) * (0.6 + 0.4 * h) / (fl + 1.5);
    }

    col = clamp(col, 0.0, 1.0);
    fragColor = vec4(col, 1.0);
}
