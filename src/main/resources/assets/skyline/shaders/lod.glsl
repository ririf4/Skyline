#version 460 core

struct ChunkData {
    uint x;
    uint z;
    uint height;
};

layout(std430, binding = 0) buffer ChunkBuffer {
    ChunkData chunks[];
};

uniform vec3 cameraPosition;

layout(location = 0) in vec3 aPos;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main() {
    uint id = gl_InstanceID;
    ChunkData chunk = chunks[id];

    vec2 chunkWorldPos = vec2(chunk.x * 16.0, chunk.z * 16.0);
    float chunkHeight = float(chunk.height);

    float distanceToCamera = distance(cameraPosition.xz, chunkWorldPos);
    int lod = int(clamp(floor(distanceToCamera / 128.0), 0.0, 4.0));

    float scale = 1.0 / pow(2.0, float(lod));
    vec3 worldPos = vec3(chunkWorldPos, 0.0) + aPos * scale * chunkHeight;

    gl_Position = projectionMatrix * viewMatrix * vec4(worldPos, 1.0);
}
