#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

// Deterministic hash — seed-based "alive" state per cell
float hash2(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

// Approximate GoL: each cell's state changes based on a time-stepped hash
// (not true GoL iteration, but produces visually similar evolving patterns)
float cellAlive(vec2 cell, float gen) {
    // Mix between two hash states to create pseudo-evolution
    float h0 = step(0.5, hash2(cell + gen * 7.3));
    float h1 = step(0.5, hash2(cell + (gen + 1.0) * 7.3));

    // Count neighbours at current generation
    float n = 0.0;
    for (int dy = -1; dy <= 1; dy++) {
        for (int dx = -1; dx <= 1; dx++) {
            if (dx == 0 && dy == 0) continue;
            n += step(0.5, hash2(cell + vec2(float(dx), float(dy)) + gen * 7.3));
        }
    }

    // Conway's rules
    float alive = h0;
    if (alive > 0.5) {
        alive = (n >= 2.0 && n <= 3.0) ? 1.0 : 0.0;
    } else {
        alive = (n == 3.0) ? 1.0 : 0.0;
    }
    return alive;
}

void main() {
    vec2 uv   = gl_FragCoord.xy / u_resolution;
    float gen = floor(u_time * 4.0);    // 4 generations per second

    // Touch: area around touch shows a "seeded" live region
    float cellSize = 10.0;
    vec2 grid = uv * u_resolution / cellSize;
    vec2 cell = floor(grid);
    vec2 frac = fract(grid);

    float alive = cellAlive(cell, gen);

    // Touch perturbs local neighbourhood
    vec2 touchCell = floor(u_touch * u_resolution / cellSize);
    float dist = length(cell - touchCell);
    if (dist < 5.0) alive = mix(alive, hash2(cell + u_time * 0.1), 0.3);

    // Colour: alive = bright green/cyan, dead = very dark
    vec3 col;
    if (alive > 0.5) {
        float age = fract(hash2(cell) + gen * 0.1);
        col = mix(vec3(0.0, 0.8, 0.2), vec3(0.0, 1.0, 0.6), age);
        // Grid lines on alive cells
        float line = smoothstep(0.05, 0.0, frac.x) + smoothstep(0.95, 1.0, frac.x)
                   + smoothstep(0.05, 0.0, frac.y) + smoothstep(0.95, 1.0, frac.y);
        col = mix(col, col * 0.4, min(line, 1.0));
    } else {
        col = vec3(0.02, 0.04, 0.03);
    }

    fragColor = vec4(col, 1.0);
}
