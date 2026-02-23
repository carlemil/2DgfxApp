#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;
    float t  = u_time * 0.7;

    float v  = sin(uv.x * 8.0 + t);
    v       += sin(uv.y * 6.0 + t * 1.3);
    v       += sin((uv.x + uv.y) * 7.0 + t * 0.9);

    // Radial wave centred on touch point
    vec2 d = uv - u_touch;
    d.x   *= u_resolution.x / u_resolution.y;
    v     += sin(length(d) * 14.0 - t * 2.2);

    float r = 0.5 + 0.5 * sin(v);
    float g = 0.5 + 0.5 * sin(v + 2.094395);
    float b = 0.5 + 0.5 * sin(v + 4.188790);

    fragColor = vec4(r, g, b, 1.0);
}
