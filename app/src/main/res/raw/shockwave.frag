#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;
    float aspect = u_resolution.x / u_resolution.y;

    // Shockwave originates from touch point
    vec2  d    = uv - u_touch;
    d.x       *= aspect;
    float dist = length(d);

    // Wave travels outward, repeating every 2 seconds
    float period  = 2.0;
    float t       = mod(u_time, period) / period;
    float radius  = t * 0.9;

    // Ring distortion
    float diff    = dist - radius;
    float ring    = smoothstep(0.07, 0.0, abs(diff)) * (1.0 - t);
    vec2  offset  = normalize(d + vec2(0.001)) * ring * 0.05 * sign(diff);

    // Sample background: concentric gradient rings
    vec2 sUV  = uv + offset;
    vec2 sD   = sUV - vec2(0.5);
    sD.x     *= aspect;
    float r2  = length(sD);

    vec3 bg   = 0.5 + 0.5 * cos(6.2832 * (vec3(0.0, 0.33, 0.67) + r2 * 2.5 - u_time * 0.2));

    // Flash at ring edge
    vec3 col  = bg + vec3(1.0) * ring * 0.6;

    fragColor = vec4(col, 1.0);
}
