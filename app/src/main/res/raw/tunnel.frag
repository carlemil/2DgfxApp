#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

void main() {
    // Centred UV, corrected for aspect ratio
    vec2 uv = (gl_FragCoord.xy - 0.5 * u_resolution) / u_resolution.y;

    // Tilt offsets the tunnel vanishing point
    uv -= vec2(u_tilt.x, -u_tilt.y) * 0.3;

    float len   = length(uv);
    float angle = atan(uv.y, uv.x);      // [-π, π]

    // Invert distance to create the tunnel projection
    float depth = 1.0 / (len + 0.001);

    // Texture coordinates: angular stripes + depth-based zoom
    float tx = angle / 6.2832 + 0.5;     // [0, 1] around the tunnel
    float ty = depth * 0.4 - u_time * 0.5;

    // Touch x shifts camera orbit
    tx += u_touch.x * 0.5;

    // Checkered tunnel walls
    float cx = fract(tx * 8.0);
    float cy = fract(ty * 8.0);
    float check = step(0.5, cx) != step(0.5, cy) ? 1.0 : 0.0;

    // Colour: cool blues, depth fog
    float fog  = 1.0 - exp(-len * 2.0);
    vec3 colA  = vec3(0.1, 0.4, 0.9);
    vec3 colB  = vec3(0.9, 0.3, 0.1);
    vec3 col   = mix(colA, colB, check);
    col        = mix(col, vec3(0.0), fog);

    fragColor = vec4(col, 1.0);
}
