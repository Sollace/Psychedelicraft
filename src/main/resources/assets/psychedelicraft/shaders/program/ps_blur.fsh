#version 150
#moj_import <psychedelicraft:utils.glsl>

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform vec2 pixelSize;
uniform float vertical;
uniform float totalAlpha;

out vec4 fragColor;

void main() {
  vec4 texel = texture(DiffuseSampler, texCoord);
  vec3 outcolor = texel.rgb;
  vec3 newColor = outcolor * 0.2;

  float xMul = (vertical == 0) ? pixelSize.x : 0.0;
  float yMul = (vertical == 1) ? pixelSize.y : 0.0;

  for (float i = -1.0; i < 2.0; i += 2.0) {
    newColor += texture(DiffuseSampler, clamp(vec2(texCoord[0] + 1.0 * i * xMul, texCoord[1] + 1.0 * i * yMul), 0.0, 1.0)).rgb * 0.15;
    newColor += texture(DiffuseSampler, clamp(vec2(texCoord[0] + 2.0 * i * xMul, texCoord[1] + 2.0 * i * yMul), 0.0, 1.0)).rgb * 0.11;
    newColor += texture(DiffuseSampler, clamp(vec2(texCoord[0] + 3.0 * i * xMul, texCoord[1] + 3.0 * i * yMul), 0.0, 1.0)).rgb * 0.09;
    newColor += texture(DiffuseSampler, clamp(vec2(texCoord[0] + 4.0 * i * xMul, texCoord[1] + 4.0 * i * yMul), 0.0, 1.0)).rgb * 0.05;
  }

  fragColor = vec4(mix(outcolor, newColor, totalAlpha), 1.0);
}
