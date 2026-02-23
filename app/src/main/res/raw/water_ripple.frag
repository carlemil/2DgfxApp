#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

uniform int  u_touch_count;
uniform vec2 u_touches[5];

out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;

    // Ripple offset — one expanding ring per touch point, plus one autonomous
    vec2 offset = vec2(0.0);

    // Autonomous ripple in the centre
    vec2  dc = uv - vec2(0.5);
    float rc = length(dc);
    offset  += normalize(dc + vec2(0.001)) * sin(rc * 30.0 - u_time * 3.5) * 0.008
              / (rc * 8.0 + 1.0);

    // Touch-driven ripples
    for (int i = 0; i < 5; i++) {
        if (i >= u_touch_count) break;
        vec2  dt = uv - u_touches[i];
        float rt = length(dt);
        offset  += normalize(dt + vec2(0.001)) * sin(rt * 28.0 - u_time * 4.0) * 0.012
                  / (rt * 6.0 + 1.0);
    }

    // Sample background: moving vertical gradient
    vec2 sampleUV = uv + offset;
    float stripe = sin(sampleUV.x * 10.0 + u_time * 0.3) * 0.5 + 0.5;
    vec3 water   = mix(vec3(0.05, 0.25, 0.55), vec3(0.3, 0.65, 0.9), stripe);

    fragColor = vec4(water, 1.0);
}
