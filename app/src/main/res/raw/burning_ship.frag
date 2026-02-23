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

    // Touch + tilt control pan and zoom
    float zoom   = exp(-u_touch.y * 3.5);
    vec2  centre = vec2(-0.5 + u_tilt.x * 0.4, -0.5 + u_tilt.y * 0.4);

    if (u_touch_count >= 2) {
        centre += (u_touches[1] - vec2(0.5)) * 2.0;
    }

    vec2 c = uv * zoom * 2.5 + centre;

    vec2  z    = vec2(0.0);
    float iter = 0.0;
    const int MAX = 96;

    for (int i = 0; i < MAX; i++) {
        if (dot(z, z) > 4.0) break;
        // Burning Ship: take absolute values before squaring
        z     = vec2(z.x * z.x - z.y * z.y + c.x,
                     2.0 * abs(z.x * z.y)   + c.y);
        iter += 1.0;
    }

    if (iter >= float(MAX)) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    float smooth_i = iter - log2(log2(dot(z, z))) + 4.0;
    float t = smooth_i / float(MAX);

    // Fiery palette: red-orange-yellow
    float offset = u_time * 0.04;
    vec3 col = 0.5 + 0.5 * cos(6.2832 * (vec3(0.0, 0.18, 0.35) + t * 2.0 + offset));

    fragColor = vec4(col, 1.0);
}
