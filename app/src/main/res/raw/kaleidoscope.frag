#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

void main() {
    vec2 uv = (gl_FragCoord.xy - 0.5 * u_resolution) / u_resolution.y;

    // Number of mirror segments (touch x changes it from 4 to 16)
    float segs = floor(4.0 + u_touch.x * 12.0);
    float PI   = 3.14159265;

    float angle = atan(uv.y, uv.x);
    float r     = length(uv);

    // Mirror into one wedge
    float wedge  = PI / segs;
    float a      = mod(angle, 2.0 * wedge);
    if (a > wedge) a = 2.0 * wedge - a;

    // Apply slight rotation over time
    a += u_time * 0.05;

    vec2 mirr = vec2(cos(a), sin(a)) * r;

    // Zoom + rotate slowly
    float scale = 3.0 + 2.0 * sin(u_time * 0.2);
    vec2  tex   = mirr * scale;
    tex  += u_time * 0.1;

    // Fractal-ish background pattern
    float p = sin(tex.x * 4.0) * sin(tex.y * 4.0)
            + 0.5 * sin(tex.x * 8.0) * sin(tex.y * 8.0);

    float hue = fract(p * 0.3 + u_time * 0.04);
    vec3 col  = 0.5 + 0.5 * cos(6.2832 * (vec3(0.0, 0.33, 0.67) + hue));

    // Radial vignette
    col *= 1.0 - smoothstep(0.4, 0.7, r);

    fragColor = vec4(col, 1.0);
}
