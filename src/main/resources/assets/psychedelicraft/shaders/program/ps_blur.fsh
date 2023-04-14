#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 pixelSize;
uniform float hBlur;
uniform float vBlur;
uniform float repeats;

out vec4 fragColor;

vec3 blurPass(vec3 outcolor, int i, float totalAlpha) {
  vec3 newColor = outcolor * 0.2;

  float xMul = (i == 0) ? pixelSize.x * hBlur : 0.0;
  float yMul = (i == 1) ? pixelSize.y * vBlur : 0.0;

  for (float i = -1.0; i < 2.0; i += 2.0) {
    newColor += texture(DiffuseSampler, clamp(vec2(texCoord[0] + 1.0 * i * xMul, texCoord[1] + 1.0 * i * yMul), 0.0, 1.0)).rgb * 0.15;
    newColor += texture(DiffuseSampler, clamp(vec2(texCoord[0] + 2.0 * i * xMul, texCoord[1] + 2.0 * i * yMul), 0.0, 1.0)).rgb * 0.11;
    newColor += texture(DiffuseSampler, clamp(vec2(texCoord[0] + 3.0 * i * xMul, texCoord[1] + 3.0 * i * yMul), 0.0, 1.0)).rgb * 0.09;
    newColor += texture(DiffuseSampler, clamp(vec2(texCoord[0] + 4.0 * i * xMul, texCoord[1] + 4.0 * i * yMul), 0.0, 1.0)).rgb * 0.05;
  }

  return mix(outcolor, newColor, totalAlpha);
}

void main() {
  vec4 texel = texture(DiffuseSampler, texCoord);
  vec3 outcolor = texel.rgb;

  for (int n = 0; n < repeats; n++) {
    float activeHBlur = min(1, hBlur - n);
    float activeVBlur = min(1, vBlur - n);

    if (activeHBlur > 0 && activeVBlur > 0) {
      outcolor = mix(blurPass(outcolor, 0, activeHBlur), blurPass(outcolor, 1, activeVBlur), 0.5);
    } else {
      if (activeHBlur > 0) {
        outcolor = blurPass(outcolor, 0, activeHBlur);
      }
      if (activeVBlur > 0) {
        outcolor = blurPass(outcolor, 1, activeVBlur);
      }
    }
  }

  fragColor = vec4(outcolor, texel.a);
}

