#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

void main() {
    vec2 uv = (gl_FragCoord.xy - 0.5 * u_resolution) / u_resolution.y;

    // Tilt & touch orbit the light
    float lightX = cos(u_time * 0.7 + u_tilt.x * 2.0 + (u_touch.x - 0.5) * 4.0);
    float lightY = sin(u_time * 0.5 + u_tilt.y * 2.0 + (u_touch.y - 0.5) * 4.0);
    vec3  light  = normalize(vec3(lightX, lightY, 1.0));

    // Ray-sphere intersection (sphere at origin, radius 0.45)
    vec3  ro     = vec3(0.0, 0.0, 2.0);   // camera origin
    vec3  rd     = normalize(vec3(uv, -1.5));
    float R      = 0.45;

    float b      = dot(rd, ro);
    float disc   = b * b - (dot(ro, ro) - R * R);

    if (disc < 0.0) {
        // Background: dark gradient
        fragColor = vec4(vec3(0.04, 0.04, 0.08), 1.0);
        return;
    }

    float t  = -b - sqrt(disc);
    vec3  p  = ro + rd * t;
    vec3  n  = normalize(p);

    // Diffuse + specular (Blinn-Phong)
    float diff = max(dot(n, light), 0.0);
    vec3  h    = normalize(light - rd);
    float spec = pow(max(dot(n, h), 0.0), 64.0);

    // Sphere surface colour: latitude/longitude grid
    float lat = acos(n.y) / 3.14159;
    float lon = atan(n.z, n.x) / 6.2832 + 0.5 + u_time * 0.05;
    float grid = smoothstep(0.03, 0.01, abs(fract(lat * 8.0) - 0.5))
               + smoothstep(0.03, 0.01, abs(fract(lon * 8.0) - 0.5));
    grid = min(grid, 1.0);

    vec3 base = mix(vec3(0.1, 0.3, 0.7), vec3(0.7, 0.2, 0.1), lat);
    vec3 col  = base * (diff * 0.8 + 0.2) + vec3(spec);
    col       = mix(col, vec3(1.0), grid * 0.5);

    fragColor = vec4(col, 1.0);
}
