#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

void main() {
    vec2 uv = (gl_FragCoord.xy - 0.5 * u_resolution) / u_resolution.y;

    // Touch x controls rotation; base rotation spins over time
    float angle = u_time * 0.6 + u_touch.x * 6.2832;
    float cosA  = cos(angle);
    float sinA  = sin(angle);
    vec2  rot   = vec2(cosA * uv.x - sinA * uv.y,
                       sinA * uv.x + cosA * uv.y);

    // Touch y controls zoom
    float scale = 6.0 + 4.0 * sin(u_time * 0.5) + (u_touch.y - 0.5) * 6.0;
    vec2 grid   = rot * scale;

    float checker = mod(floor(grid.x) + floor(grid.y), 2.0);

    // Two complementary hues that cycle
    float hShift = u_time * 0.05;
    vec3 colA = 0.5 + 0.5 * cos(6.2832 * (vec3(0.0, 0.33, 0.67) + hShift));
    vec3 colB = 0.5 + 0.5 * cos(6.2832 * (vec3(0.5, 0.83, 0.17) + hShift));
    vec3 col  = mix(colA, colB, checker);

    // Vignette
    float vign = 1.0 - smoothstep(0.5, 1.0, length(uv));
    col *= vign;

    fragColor = vec4(col, 1.0);
}
