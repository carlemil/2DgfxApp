#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

void main() {
    vec2 uv = (gl_FragCoord.xy - 0.5 * u_resolution) / u_resolution.y;

    // Touch shifts camera offset
    uv -= (u_touch - vec2(0.5)) * 0.4;

    float len   = length(uv);
    float angle = atan(uv.y, uv.x);

    // Tunnel projection
    float depth = 0.3 / (len + 0.001);
    float t     = u_time * 0.6;

    // Grid in angular-depth space
    float rings  = 10.0;
    float spokes = 14.0;
    float ringCoord  = fract(depth - t) * rings;
    float spokeCoord = fract(angle / 6.2832 * spokes);

    // Draw dots at grid intersections
    float dr = ringCoord  - round(ringCoord);
    float ds = spokeCoord - round(spokeCoord);
    float dot = smoothstep(0.25, 0.1, length(vec2(dr, ds)) * 2.0);

    // Colour by depth and angle
    float hue = angle / 6.2832 + 0.5 + depth * 0.1;
    vec3 col  = 0.5 + 0.5 * cos(6.2832 * (vec3(0.0, 0.33, 0.67) + hue));
    col      *= dot;

    // Fog in the centre (far end)
    col = mix(col, vec3(0.0), 1.0 - smoothstep(0.05, 0.5, len));

    fragColor = vec4(col, 1.0);
}
