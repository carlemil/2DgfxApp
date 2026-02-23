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
    return mix(mix(hash(i), hash(i+vec2(1,0)), f.x),
               mix(hash(i+vec2(0,1)), hash(i+vec2(1,1)), f.x), f.y);
}

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;

    // Tilt affects haze intensity and direction
    float tiltX = u_tilt.x * 0.5;
    float tiltY = u_tilt.y * 0.5;

    // Rising heat distortion
    float t = u_time * 0.35;
    vec2 haze;
    haze.x = noise(vec2(uv.x * 4.0 + tiltX, uv.y * 6.0 - t * 1.2)) - 0.5;
    haze.y = noise(vec2(uv.x * 3.5 - tiltY, uv.y * 5.0 - t * 1.5)) - 0.5;

    float strength = 0.025 * (1.0 - uv.y);   // strongest at bottom
    vec2  distUV   = uv + haze * strength;

    // Background: desert sky gradient with a "ground" at the bottom
    float sky = smoothstep(0.3, 0.7, distUV.y);
    vec3 skyCol  = mix(vec3(0.85, 0.65, 0.3), vec3(0.4, 0.6, 0.9), sky);
    vec3 groundCol = vec3(0.7, 0.55, 0.25);
    vec3 bg   = mix(groundCol, skyCol, smoothstep(0.25, 0.35, distUV.y));

    // Heat shimmer brightening
    float shimmer = abs(haze.x + haze.y) * 3.0;
    bg += shimmer * vec3(0.3, 0.2, 0.05);

    fragColor = vec4(bg, 1.0);
}
