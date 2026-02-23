#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

// Simple hash for pseudo-random pattern used as "text" background
float hash(float n) { return fract(sin(n) * 43758.5453); }

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;

    // Sine ribbon position
    float freq  = 3.0 + u_touch.x * 3.0;
    float ribbY = 0.5 + 0.28 * sin(uv.x * freq + u_time * 2.0);
    float dist  = abs(uv.y - ribbY);

    // Ribbon thickness
    float ribbon = smoothstep(0.07, 0.01, dist);

    // Rainbow across ribbon width
    float hue = fract(uv.x * 0.5 - u_time * 0.08);
    float h6  = hue * 6.0;
    float c   = 1.0 - abs(mod(h6, 2.0) - 1.0);
    vec3  rgb;
    if      (h6 < 1.0) rgb = vec3(1.0, c,   0.0);
    else if (h6 < 2.0) rgb = vec3(c,   1.0, 0.0);
    else if (h6 < 3.0) rgb = vec3(0.0, 1.0, c  );
    else if (h6 < 4.0) rgb = vec3(0.0, c,   1.0);
    else if (h6 < 5.0) rgb = vec3(c,   0.0, 1.0);
    else               rgb = vec3(1.0, 0.0, c  );

    // Dark scanline background
    float scan = 0.5 + 0.5 * sin(uv.y * u_resolution.y * 0.5);
    vec3 bg = vec3(0.0, 0.0, 0.07) * scan;

    // Soft glow around ribbon
    vec3 glow = rgb * smoothstep(0.18, 0.03, dist) * 0.35;

    fragColor = vec4(bg + rgb * ribbon + glow, 1.0);
}
