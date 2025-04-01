#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:matrix.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform float GameTime;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec4 texProj0;
in vec4 vertexColor;
in float vertexDistance;

const mat4 SCALE_TRANSLATE = mat4(
  0.5, 0.0, 0.0, 0.25,
  0.0, 0.5, 0.0, 0.25,
  0.0, 0.0, 1.0, 0.0,
  0.0, 0.0, 0.0, 1.0
);

out vec4 fragColor;

void main() {
  vec4 scale = vec4(1.25, 1.25, 0.25, 0.25);
  vec3 color = textureProj(Sampler0, texProj0 * scale).rgb;

  fragColor = linear_fog(vec4(color, vertexColor.a), vertexDistance, FogStart, FogEnd, FogColor);
}
