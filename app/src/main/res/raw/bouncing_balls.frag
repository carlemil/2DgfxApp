#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

float hash(float n) { return fract(sin(n) * 43758.5453); }

float ball(vec2 uv, vec2 pos, float r) {
    return smoothstep(r, r * 0.6, length(uv - pos));
}

void main() {
    vec2 uv  = gl_FragCoord.xy / u_resolution;
    float ar = u_resolution.x / u_resolution.y;
    vec2 uvA = uv;
    uvA.x   *= ar;

    // Gravity from tilt
    float gx = u_tilt.x * 0.4;
    float gy = -u_tilt.y * 0.4;

    vec3 col = vec3(0.05, 0.05, 0.1);

    int N = 8;
    for (int i = 0; i < N; i++) {
        float fi   = float(i);
        float seed = fi * 13.37;

        // Bouncing trajectory
        float px  = hash(seed)       * ar;
        float py  = hash(seed + 1.0);
        float vx  = (hash(seed + 2.0) * 2.0 - 1.0) * 0.3;
        float vy  = (hash(seed + 3.0) * 2.0 - 1.0) * 0.25;
        float t   = u_time * (0.5 + hash(seed + 4.0) * 0.7);

        // Simple physics with gravity and wall bounce
        float bx = fract((px + vx * t + gx * t * t * 0.5) * 0.5) * ar;
        float by = abs(sin(vy * t + gx * t));
        by = fract((py + vy * t + gy * t * t * 0.5) * 0.5);
        by = 1.0 - abs(fract(by) * 2.0 - 1.0);   // bounce

        // Touch: attract nearest ball
        float distToTouch = length(vec2(u_touch.x * ar, u_touch.y) - vec2(bx, by));
        if (distToTouch < 0.15) {
            vec2 attract = normalize(vec2(u_touch.x * ar - bx, u_touch.y - by));
            bx += attract.x * 0.02;
            by += attract.y * 0.02;
        }

        float r   = 0.04 + 0.02 * hash(seed + 5.0);
        float lit = ball(uvA, vec2(bx, by), r);

        float hue = hash(seed + 6.0) + u_time * 0.03;
        vec3 c = 0.5 + 0.5 * cos(6.2832 * (vec3(0.0, 0.33, 0.67) + hue));
        col += c * lit;
    }

    col = clamp(col, 0.0, 1.0);
    fragColor = vec4(col, 1.0);
}
