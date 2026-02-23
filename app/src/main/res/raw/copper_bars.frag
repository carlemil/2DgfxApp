#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;

    // Bars scroll downward, slight horizontal sine wobble
    float t   = u_time * 0.4;
    float bar = uv.y + sin(uv.x * 3.1 + t * 1.5) * 0.04 + t;
    float pal = fract(bar * 4.0);

    // Copper gradient: dark-red → orange → bright gold
    vec3 col  = mix(vec3(0.55, 0.12, 0.0), vec3(1.0, 0.65, 0.1), pal);
    col = mix(col, vec3(1.0, 1.0, 0.85), smoothstep(0.7, 0.95, pal));

    // Dark gap between bars
    float edge = fract(bar * 8.0);
    float fade = smoothstep(0.0, 0.12, edge) * smoothstep(1.0, 0.88, edge);
    col = mix(vec3(0.02, 0.0, 0.0), col, fade);

    fragColor = vec4(col, 1.0);
}
