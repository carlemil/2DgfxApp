#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

// Up to 5 simultaneous touch points forwarded from MetaballsEffect
uniform int   u_touch_count;
uniform vec2  u_touches[5];

out vec4 fragColor;

float metaball(vec2 uv, vec2 centre, float r) {
    float d = length(uv - centre);
    return r / (d * d + 0.0001);
}

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;

    // Animated autonomous balls
    float t = u_time;
    vec2 b[4];
    b[0] = vec2(0.5 + 0.3 * cos(t * 1.10),  0.5 + 0.3 * sin(t * 0.90));
    b[1] = vec2(0.5 + 0.25 * cos(t * 0.73 + 1.5), 0.5 + 0.25 * sin(t * 1.13 + 0.7));
    b[2] = vec2(0.5 + 0.28 * sin(t * 0.88 + 2.1), 0.5 + 0.28 * cos(t * 0.95 + 1.3));
    b[3] = vec2(0.5 + 0.22 * cos(t * 1.31 + 3.0), 0.5 + 0.22 * sin(t * 0.77 + 2.2));

    float field = 0.0;
    field += metaball(uv, b[0], 0.018);
    field += metaball(uv, b[1], 0.015);
    field += metaball(uv, b[2], 0.013);
    field += metaball(uv, b[3], 0.012);

    // Add touch blobs
    for (int i = 0; i < 5; i++) {
        if (i >= u_touch_count) break;
        field += metaball(uv, u_touches[i], 0.022);
    }

    float iso   = smoothstep(0.9, 1.1, field);
    vec3  inner = vec3(0.2, 0.8, 0.6);
    vec3  outer = vec3(0.05, 0.05, 0.12);
    vec3  col   = mix(outer, inner, iso);

    // Specular highlight at field peak
    float spec = smoothstep(1.4, 1.8, field) * 0.7;
    col       += spec;

    fragColor = vec4(col, 1.0);
}
