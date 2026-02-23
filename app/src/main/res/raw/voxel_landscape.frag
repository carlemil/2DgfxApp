#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

// Simple value noise
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}
float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i),           hash(i + vec2(1,0)), f.x),
               mix(hash(i + vec2(0,1)), hash(i + vec2(1,1)), f.x), f.y);
}
float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) { v += a * noise(p); p *= 2.1; a *= 0.5; }
    return v;
}

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;

    // Ray direction — slight tilt influence on view angle
    float pitch = -0.4 + u_tilt.y * 0.3 + (u_touch.y - 0.5) * 0.4;
    float yaw   = u_time * 0.15 + u_tilt.x * 0.5 + (u_touch.x - 0.5) * 0.6;

    // Camera sweeps forward
    vec2 camPos = vec2(yaw, u_time * 0.6);

    // Ray marching across the height map
    float col1 = 0.0;
    float minH = 1.0;
    float y    = uv.y + pitch;

    for (int i = 0; i < 60; i++) {
        float t    = float(i) / 60.0 + 0.02;
        vec2  pos  = camPos + vec2(cos(yaw), sin(yaw)) * t
                            + vec2(-sin(yaw), cos(yaw)) * (uv.x - 0.5) * t;
        float h    = fbm(pos * 1.5) * 0.5;
        float projH = (h - 0.15) / t * 0.7;
        if (projH > y && h < minH) {
            minH = h;
            col1 = 1.0 - t;   // closer = brighter
        }
    }

    // Sky gradient
    vec3 sky  = mix(vec3(0.5, 0.7, 1.0), vec3(0.1, 0.2, 0.5), uv.y * 1.5);
    // Terrain colour: green→brown→snow by height
    vec3 land = mix(vec3(0.15, 0.45, 0.1), vec3(0.5, 0.35, 0.2), minH * 1.5);
    land = mix(land, vec3(0.9, 0.9, 1.0), smoothstep(0.6, 0.8, minH));
    land *= col1 * 1.3 + 0.2;

    vec3 col = mix(sky, land, smoothstep(0.0, 0.05, col1));

    fragColor = vec4(col, 1.0);
}
