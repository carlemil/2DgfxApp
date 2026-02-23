#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

float hash(float n) { return fract(sin(n) * 43758.5453); }

void main() {
    vec2 uv  = gl_FragCoord.xy / u_resolution;
    vec3 col = vec3(0.0);

    // Several simultaneous bursts
    int BURSTS = 5;
    for (int b = 0; b < BURSTS; b++) {
        float fb   = float(b);
        float seed = fb * 13.7;
        float period = 1.5 + hash(seed) * 2.0;
        float phase  = mod(u_time + hash(seed + 1.0) * period, period);

        // Burst centre (touch drives one burst)
        vec2 centre;
        if (b == 0) {
            centre = u_touch;
        } else {
            centre = vec2(hash(seed + 2.0), 0.2 + hash(seed + 3.0) * 0.7);
        }

        // Expand and fade
        float radius  = phase * 0.45;
        float fade    = 1.0 - phase / period;
        int   SPARKS  = 24;

        for (int s = 0; s < SPARKS; s++) {
            float fs    = float(s);
            float angle = fs / float(SPARKS) * 6.2832 + hash(seed + fs) * 0.4;
            float r2    = radius * (0.8 + 0.2 * hash(seed + fs + 100.0));
            vec2  sPos  = centre + vec2(cos(angle), sin(angle)) * r2;
            // Gravity: sparks fall
            sPos.y     -= phase * phase * 0.15;

            float sz    = 0.007 * (1.0 - phase / period);
            float d     = length(uv - sPos);
            float spot  = smoothstep(sz, 0.0, d);

            float hue   = hash(seed + 200.0);
            vec3 c = 0.5 + 0.5 * cos(6.2832 * (vec3(0.0, 0.33, 0.67) + hue));
            col += c * spot * fade;
        }
    }

    col = min(col, vec3(1.0));
    fragColor = vec4(col, 1.0);
}
