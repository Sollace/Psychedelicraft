#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in vec4 texProj0;
in vec4 vertexColor;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;

out vec4 fragColor;

void main() {
  vec3 color = textureProj(Sampler0, texProj0).rgb;

  fragColor = apply_fog(vec4(color, vertexColor.a),
    sphericalVertexDistance,
    cylindricalVertexDistance,
    FogEnvironmentalStart, FogEnvironmentalEnd,
    FogRenderDistanceStart, FogRenderDistanceEnd,
    FogColor
  );
}
