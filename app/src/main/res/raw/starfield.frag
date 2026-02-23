#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

float hash(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }

void main() {
    vec2 uv = (gl_FragCoord.xy - 0.5 * u_resolution) / u_resolution.y;

    // Tilt steers the starfield
    float speed = 0.5;
    vec2 vel = vec2(u_tilt.x * 0.4, -u_tilt.y * 0.4);

    vec3 col = vec3(0.0);

    // Multiple depth layers
    for (int layer = 0; layer < 4; layer++) {
        float fl = float(layer);
        float scale = pow(2.0, fl);
        float t     = u_time * speed * (fl + 1.0);

        vec2 pos = uv * scale - vel * t;
        vec2 cell = floor(pos);
        vec2 frac = fract(pos);

        float h  = hash(cell + fl * 7.3);
        float sz = 0.3 + 0.7 * h;
        float br = 0.2 + 0.8 * hash(cell + fl * 3.1);  // brightness

        float d  = length(frac - vec2(0.5));
        float star = smoothstep(0.15 * sz, 0.0, d);

        // Colour: mostly white, occasional colour tint
        float hue = hash(cell + fl * 5.9);
        vec3 c = mix(vec3(1.0), 0.5 + 0.5 * cos(6.2832 * (vec3(0.0,0.33,0.67) + hue)), 0.3);
        col += c * star * br / (fl + 1.0);
    }

    col = clamp(col * 1.5, 0.0, 1.0);
    fragColor = vec4(col, 1.0);
}
