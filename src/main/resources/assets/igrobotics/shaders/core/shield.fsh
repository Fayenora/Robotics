#version 150

#moj_import <fog.glsl>

in vec3 color;
in float fresnel;
in float vertexDistance;

uniform float FogStart;
uniform float FogEnd;

out vec4 fragColor;

void main() {
    fragColor = mix(vec4(color.xyz, 0.5), vec4(1, 1, 1, 0.5), fresnel) * linear_fog_fade(vertexDistance, FogStart, FogEnd);
}