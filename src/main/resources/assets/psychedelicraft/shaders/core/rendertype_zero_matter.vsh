#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;

out vec4 texProj0;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;

void main() {
  gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

  vertexColor = Color;
  texProj0 = projection_from_position(gl_Position);
  sphericalVertexDistance = fog_spherical_distance(Position);
  cylindricalVertexDistance = fog_cylindrical_distance(Position);
}
