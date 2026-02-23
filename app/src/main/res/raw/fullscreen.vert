#version 300 es
// Fullscreen triangle — drawn with glDrawArrays(GL_TRIANGLES, 0, 3)
// No vertex buffer needed; positions are computed from gl_VertexID.

out vec2 v_uv;  // [0,1] UV passed to fragment shader

void main() {
    // Map vertex indices 0,1,2 → positions that cover [-1,-1]..[1,1]
    vec2 pos = vec2(
        (gl_VertexID == 1) ? 3.0 : -1.0,
        (gl_VertexID == 2) ? 3.0 : -1.0
    );
    v_uv        = pos * 0.5 + 0.5;   // remap [-1,1] → [0,1]
    gl_Position = vec4(pos, 0.0, 1.0);
}
