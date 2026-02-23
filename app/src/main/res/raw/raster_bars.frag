#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

vec3 hue2rgb(float h) {
    float h6 = fract(h) * 6.0;
    float c  = 1.0 - abs(mod(h6, 2.0) - 1.0);
    if      (h6 < 1.0) return vec3(1.0, c,   0.0);
    else if (h6 < 2.0) return vec3(c,   1.0, 0.0);
    else if (h6 < 3.0) return vec3(0.0, 1.0, c  );
    else if (h6 < 4.0) return vec3(0.0, c,   1.0);
    else if (h6 < 5.0) return vec3(c,   0.0, 1.0);
    else               return vec3(1.0, 0.0, c  );
}

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;

    // Bars scroll upward; palette shifts over time
    float t    = u_time * 0.5;
    float y    = uv.y - t * 0.12;
    float barN = floor(y * 10.0);
    float pal  = fract(barN * 0.2731 + t * 0.07);   // staggered hue per bar

    // Band edge fade
    float edge = fract(y * 10.0);
    float fade = smoothstep(0.0, 0.1, edge) * smoothstep(1.0, 0.9, edge);

    vec3 col = hue2rgb(pal) * fade;

    fragColor = vec4(col, 1.0);
}
