#version 150

#moj_import <fog.glsl>

in vec3 color;
in float vertexDistance;

uniform float FogStart;
uniform float FogEnd;

out vec4 fragColor;

void main() {
    fragColor = vec4(color.xyz, 0.5) * linear_fog_fade(vertexDistance, FogStart, FogEnd);
}