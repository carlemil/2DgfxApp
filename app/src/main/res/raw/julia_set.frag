#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

uniform int   u_touch_count;
uniform vec2  u_touches[5];

out vec4 fragColor;

void main() {
    vec2 uv = (gl_FragCoord.xy - 0.5 * u_resolution) / u_resolution.y;

    // Julia parameter C is driven by touch + time orbit
    vec2 c;
    if (u_touch_count > 0) {
        // First touch finger directly controls C
        c = (u_touches[0] - vec2(0.5)) * 3.0;
    } else {
        // Autonomous orbit around interesting regions
        c = vec2(
            0.7885 * cos(u_time * 0.2),
            0.7885 * sin(u_time * 0.2)
        );
    }

    // Zoom controlled by second touch or tilt
    float zoom  = 1.5 + u_tilt.y * 0.5;
    if (u_touch_count >= 2) zoom = 1.0 + length(u_touches[1] - u_touches[0]) * 2.0;

    vec2  z   = uv * zoom * 2.0;
    float iter = 0.0;
    const int MAX = 96;

    for (int i = 0; i < MAX; i++) {
        if (dot(z, z) > 4.0) break;
        z     = vec2(z.x * z.x - z.y * z.y + c.x,
                     2.0 * z.x * z.y         + c.y);
        iter += 1.0;
    }

    if (iter >= float(MAX)) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    float smooth_i = iter - log2(log2(dot(z, z))) + 4.0;
    float t = smooth_i / float(MAX);

    float offset = u_time * 0.07;
    vec3 col = 0.5 + 0.5 * cos(6.2832 * (vec3(0.15, 0.45, 0.73) + t * 2.5 + offset));

    fragColor = vec4(col, 1.0);
}
