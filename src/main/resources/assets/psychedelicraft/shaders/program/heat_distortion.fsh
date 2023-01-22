#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform sampler2D NoiseSampler;

in vec2 texCoord;

uniform float totalAlpha;
uniform float ticks;
uniform float strength;

out vec4 fragColor;

void main() {
  vec4 depthPixel = texture(DepthSampler, texCoord.st);
  vec4 noisePixel1 = texture(NoiseSampler, texCoord.st * 4.0 + vec2(ticks * 0.324823048, ticks * 0.48913801));
  vec4 noisePixel2 = texture(NoiseSampler, texCoord.ts * 4.0 + vec2(ticks * 0.52890348, ticks * 0.6318212));

  vec4 joinedNoise = noisePixel1 + noisePixel2 - 1.0;

  float depthMul = min(0.4 / sqrt(sqrt(sqrt(1.0 - depthPixel.r))) - 0.4, 1.0);
  vec4 newColor = texture(DiffuseSampler, clamp(texCoord.st + joinedNoise.rg * strength * depthMul, 0.0, 1.0));

  if (totalAlpha == 1.0) {
    fragColor = newColor;
  } else {
    fragColor = mix(texture(DiffuseSampler, texCoord.st), newColor, totalAlpha);
  }
}
