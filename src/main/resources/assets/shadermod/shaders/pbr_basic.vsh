#version 150 core

in vec3 Position;
in vec3 Color;
in vec2 TexCoord;
in vec3 Normal;

out vec3 vColor;
out vec2 vTexCoord;
out vec3 vNormal;
out vec3 vPosition;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 NormalMat;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vColor = Color;
    vTexCoord = TexCoord;
    vNormal = normalize(NormalMat * Normal);
    vPosition = Position;
}
