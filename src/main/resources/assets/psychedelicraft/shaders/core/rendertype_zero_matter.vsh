#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;

out vec4 texProj0;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;

const float BITS_DISTANCE = 1.25;
const float BITS_SIZE = 0.125;
const vec4 SCALE = vec4(BITS_DISTANCE, -BITS_DISTANCE, BITS_SIZE, BITS_SIZE);
const float FRAME_WIDTH = 0.125; // 1/8

void main() {
  gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

  vertexColor = Color;
  float frameIndex = floor(GameTime * 24000 / 2) * FRAME_WIDTH;

  vec4 scaledScreenUv = gl_Position;

  texProj0 = projection_from_position(scaledScreenUv * SCALE);
  
  float u = texProj0.x / texProj0.w;
  
  texProj0.x = (u * FRAME_WIDTH + frameIndex) * texProj0.w;
  
  sphericalVertexDistance = fog_spherical_distance(Position);
  cylindricalVertexDistance = fog_cylindrical_distance(Position);
}
