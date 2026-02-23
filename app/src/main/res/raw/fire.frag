#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

float hash(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i),             hash(i + vec2(1,0)), f.x),
               mix(hash(i + vec2(0,1)), hash(i + vec2(1,1)), f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0, a = 0.5;
    for (int i = 0; i < 5; i++) { v += a * noise(p); p *= 2.1; a *= 0.5; }
    return v;
}

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;
    uv.x   += (u_touch.x - 0.5) * 0.4;   // touch shifts base

    // Rising turbulence
    float t = u_time * 0.5;
    vec2 fPos = vec2(uv.x * 2.5, (1.0 - uv.y) * 2.5 - t);
    float n   = fbm(fPos);

    // Height mask: fire is brighter at bottom, fades upward
    float mask = (1.0 - uv.y) * (1.0 - uv.y);

    float fire = n * mask * 2.2;
    fire = clamp(fire, 0.0, 1.0);

    // Fire palette: black → red → orange → yellow → white
    vec3 col;
    if      (fire < 0.3) col = mix(vec3(0.0), vec3(0.8, 0.0, 0.0), fire / 0.3);
    else if (fire < 0.6) col = mix(vec3(0.8, 0.0, 0.0), vec3(1.0, 0.5, 0.0), (fire - 0.3) / 0.3);
    else if (fire < 0.85) col = mix(vec3(1.0, 0.5, 0.0), vec3(1.0, 1.0, 0.2), (fire - 0.6) / 0.25);
    else                 col = mix(vec3(1.0, 1.0, 0.2), vec3(1.0, 1.0, 1.0), (fire - 0.85) / 0.15);

    fragColor = vec4(col, 1.0);
}
