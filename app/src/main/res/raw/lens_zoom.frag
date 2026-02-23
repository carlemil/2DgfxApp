#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;

    // Lens centre tracks touch
    vec2 centre = u_touch;

    vec2  d    = uv - centre;
    float dist = length(d);

    // Lens radius oscillates gently
    float radius = 0.25 + 0.1 * sin(u_time * 0.7);

    // Inside lens: magnification (barrel)
    vec2 lensUV;
    if (dist < radius) {
        float strength = 1.5;
        float t = dist / radius;
        lensUV = centre + normalize(d + vec2(0.001)) * pow(t, strength) * radius;
    } else {
        lensUV = uv;
    }

    // Underlying pattern: psychedelic grid
    float px = lensUV.x * 12.0 + u_time * 0.2;
    float py = lensUV.y * 12.0 + u_time * 0.15;
    float pat = sin(px) * sin(py) * 0.5 + 0.5;

    // Colour
    vec3 col = 0.5 + 0.5 * cos(6.2832 * (vec3(0.0, 0.33, 0.67) + pat + u_time * 0.05));

    // Lens rim highlight
    float rim = smoothstep(0.02, 0.0, abs(dist - radius));
    col = mix(col, vec3(1.0), rim * 0.5);

    fragColor = vec4(col, 1.0);
}
