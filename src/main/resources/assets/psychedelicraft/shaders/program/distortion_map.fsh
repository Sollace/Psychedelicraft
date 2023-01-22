#version 120

uniform sampler2D DiffuseSampler;

uniform sampler2D OverlaySampler1;
uniform sampler2D OverlaySampler2;

in vec2 texCoord;

uniform float totalAlpha;
uniform float strength;
uniform vec2 texTranslation0;
uniform vec2 texTranslation1;

out vec4 fragColor;

void main() {
  vec4 noisePixel0 = texture(OverlaySampler1, texCoord + texTranslation0);
  vec4 noisePixel1 = texture(OverlaySampler2, texCoord + texTranslation1);
  vec2 joinedTranslation = clamp(noisePixel0.rg + noisePixel1.rg - 1.0, 0.0, 1.0);

  vec2 water1 = abs(noisePixel0.rg - 0.5) * 2.0;
  joinedTranslation *= mix(vec2(1.0), water1, noisePixel0.b);

  vec2 water2 = abs(noisePixel1.rg - 0.5) * 2.0;
  joinedTranslation *= mix(vec2(1.0), water2, noisePixel1.b);

  vec4 newColor = texture(DiffuseSampler, texCoord + joinedTranslation * strength);

	if (totalAlpha == 1.0) {
	  fragColor = newColor;
	} else {
	  fragColor = mix(texture(DiffuseSampler, texCoord.st), newColor, totalAlpha);
  }
}
