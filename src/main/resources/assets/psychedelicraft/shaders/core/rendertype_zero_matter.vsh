#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out vec4 texProj0;
out float vertexDistance;
out vec4 vertexColor;

void main() {
  gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

  vertexColor = Color * texelFetch(Sampler0, UV2 / 16, 0);
  texProj0 = projection_from_position(gl_Position);
  vertexDistance = fog_distance(Position, FogShape);
}
