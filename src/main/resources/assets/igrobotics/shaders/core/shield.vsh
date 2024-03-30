#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec3 Normal;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;
uniform vec3 CameraPos;
uniform mat3 IViewRotMat;
uniform int FogShape;
uniform vec3 Color;
uniform int Hit;
uniform float Strength;

out vec4 color;
out float fresnel;
out float vertexDistance;

void main() {
    gl_Position = ProjMat * vec4(Position, 1.0);

    // Source https://kylehalladay.com/blog/tutorial/2014/02/18/Fresnel-Shaders-From-The-Ground-Up.html
    vec3 worldPos = IViewRotMat * Position + CameraPos;
    vec3 worldNormal = normalize(IViewRotMat * Normal);
    vec3 vertexCameraDir = normalize(worldPos - CameraPos);
    fresnel = 0.7 * pow(1.0 + dot(vertexCameraDir, worldNormal), 4);

    vertexDistance = fog_distance(ModelViewMat, IViewRotMat * Position, FogShape);
    color = vec4(mix(Color, vec3(1, 1, 1), 0.2 * float(Hit > 0)), 0.1 + 0.3 * Strength);
}