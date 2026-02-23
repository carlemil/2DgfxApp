#version 300 es
precision highp float;

uniform float u_time;
uniform vec2  u_resolution;
uniform vec2  u_touch;
uniform vec3  u_tilt;

out vec4 fragColor;

// Rotation helpers
mat3 rotX(float a) { float c=cos(a),s=sin(a); return mat3(1,0,0, 0,c,-s, 0,s,c); }
mat3 rotY(float a) { float c=cos(a),s=sin(a); return mat3(c,0,s, 0,1,0, -s,0,c); }

// Box SDF
float box(vec3 p, vec3 b) {
    vec3 q = abs(p) - b;
    return length(max(q, 0.0)) + min(max(q.x, max(q.y, q.z)), 0.0);
}

float scene(vec3 p) { return box(p, vec3(0.6)); }

void main() {
    vec2 uv = (gl_FragCoord.xy - 0.5 * u_resolution) / u_resolution.y;

    // Camera
    vec3 ro = vec3(0.0, 0.0, 2.5);
    vec3 rd = normalize(vec3(uv, -1.5));

    // Spin from time + touch + tilt
    float yaw   = u_time * 0.5 + (u_touch.x - 0.5) * 4.0 + u_tilt.x * 2.0;
    float pitch = u_time * 0.3 + (u_touch.y - 0.5) * 3.0 + u_tilt.y * 2.0;
    mat3 rot = rotX(pitch) * rotY(yaw);

    vec3 col = vec3(0.07, 0.07, 0.1);

    float t = 0.0;
    for (int i = 0; i < 80; i++) {
        vec3 p  = rot * (ro + rd * t);
        float d = scene(p);
        if (d < 0.001) {
            // Normal by finite difference
            float e = 0.001;
            vec3 n = normalize(vec3(
                scene(rot*(ro+rd*t+vec3(e,0,0))) - scene(rot*(ro+rd*t-vec3(e,0,0))),
                scene(rot*(ro+rd*t+vec3(0,e,0))) - scene(rot*(ro+rd*t-vec3(0,e,0))),
                scene(rot*(ro+rd*t+vec3(0,0,e))) - scene(rot*(ro+rd*t-vec3(0,0,e)))
            ));
            vec3 ld  = normalize(vec3(1.0, 2.0, 2.0));
            float diff = max(dot(n, ld), 0.0);
            float spec = pow(max(dot(reflect(-ld, n), -rd), 0.0), 32.0);

            // Face colours based on normal
            col = abs(n) * diff * 1.2 + spec * 0.5;
            break;
        }
        t += d;
        if (t > 10.0) break;
    }

    fragColor = vec4(col, 1.0);
}
