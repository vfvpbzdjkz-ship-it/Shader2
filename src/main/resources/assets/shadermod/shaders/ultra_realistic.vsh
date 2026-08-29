#version 150 core

in vec3 Position;
in vec3 Color;
in vec2 TexCoord;
in vec3 Normal;

out vec3 vColor;
out vec2 vTexCoord;
out vec3 vNormal;
out vec3 vPosition;
out vec3 vWorldPosition;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 NormalMat;

// For multi-pass rendering support
out vec3 vViewDir;
out vec3 vLightDir;

uniform vec3 CameraPosition;
uniform vec3 LightDirection;

void main() {
    vec4 worldPos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * worldPos;
    
    vColor = Color;
    vTexCoord = TexCoord;
    vNormal = normalize(NormalMat * Normal);
    vPosition = Position;
    vWorldPosition = worldPos.xyz;
    vViewDir = normalize(CameraPosition - worldPos.xyz);
    vLightDir = normalize(LightDirection);
}
