#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

uniform int  u_touch_count;
uniform vec2 u_touches[5];

out vec4 fragColor;

// Smooth min (k = softness)
float smin(float a, float b, float k) {
    float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
    return mix(b, a, h) - k * h * (1.0 - h);
}

float blobSDF(vec2 uv, vec2 c, float r) {
    return length(uv - c) - r;
}

void main() {
    vec2 uv  = (gl_FragCoord.xy - 0.5 * u_resolution) / u_resolution.y;
    float t  = u_time * 0.55;

    // Autonomous blobs
    vec2 c0 = vec2(0.28 * cos(t * 1.0),  0.28 * sin(t * 1.3));
    vec2 c1 = vec2(0.28 * cos(t * 1.2 + 2.0), 0.28 * sin(t * 0.9 + 1.0));
    vec2 c2 = vec2(0.28 * cos(t * 0.8 + 4.0), 0.28 * sin(t * 1.1 + 3.0));

    float d = blobSDF(uv, c0, 0.18);
    d = smin(d, blobSDF(uv, c1, 0.15), 0.12);
    d = smin(d, blobSDF(uv, c2, 0.13), 0.12);

    // Touch blobs (converted from [0,1] to centred coords)
    for (int i = 0; i < 5; i++) {
        if (i >= u_touch_count) break;
        vec2 tc = (u_touches[i] - vec2(0.5)) * vec2(1.0, u_resolution.y / u_resolution.x);
        d = smin(d, blobSDF(uv, tc, 0.12), 0.1);
    }

    float iso = smoothstep(0.01, -0.02, d);
    float rim = smoothstep(0.05, 0.0, d) - iso;

    vec3 inner = 0.5 + 0.5 * cos(6.2832 * (vec3(0.0, 0.33, 0.67) + t * 0.07));
    vec3 col   = inner * iso + vec3(1.0) * rim * 0.6;
    col        = mix(vec3(0.04, 0.02, 0.08), col, iso + rim);

    fragColor = vec4(col, 1.0);
}
