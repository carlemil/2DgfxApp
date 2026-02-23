#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

float hash(float n) { return fract(sin(n) * 43758.5453); }
float hash2(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;
    vec3 col = vec3(0.0);

    int  N = 80;
    float t = u_time;

    for (int i = 0; i < N; i++) {
        float fi = float(i);
        float seed = fi * 0.137;

        // Each particle has random initial angle and speed
        float angle = hash(seed)       * 6.2832;
        float speed = hash(seed + 1.0) * 0.35 + 0.05;
        float life  = hash(seed + 2.0) * 2.5 + 0.5;   // lifetime in seconds

        // Phase within lifetime
        float phase = mod(t * speed + hash(seed + 3.0) * life, life) / life;  // [0,1]

        // Attraction: move toward touch or tilt direction
        float attractX = u_touch.x - 0.5 + u_tilt.x * 0.3;
        float attractY = u_touch.y - 0.5 - u_tilt.y * 0.3;
        vec2 attract = normalize(vec2(attractX, attractY) + vec2(sin(angle), cos(angle)) * 0.001);

        vec2 pos = vec2(0.5, 0.5)
                 + attract * phase * 0.5 * speed
                 + vec2(sin(angle), cos(angle)) * (1.0 - phase) * 0.35;

        float sz  = mix(0.012, 0.002, phase);
        float d   = length(uv - pos);
        float spot = smoothstep(sz, sz * 0.3, d);

        float hue = hash(seed + 4.0) + t * 0.1;
        vec3 c = 0.5 + 0.5 * cos(6.2832 * (vec3(0.0, 0.33, 0.67) + hue));
        col += c * spot * (1.0 - phase);
    }

    col = min(col, vec3(1.0));
    fragColor = vec4(col, 1.0);
}
