#version 150
#moj_import <psychedelicraft:random_from_vec.glsl>
#moj_import <psychedelicraft:random_from_seed.glsl>

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform vec2 pixelSize;
uniform float totalAlpha;
uniform float seed;
uniform float strength;

out vec4 fragColor;

void main() {
	vec2 newTexCoords = floor(texCoord / pixelSize) * pixelSize;
	newTexCoords.t += (mod(randomFromSeed(seed), 1.0) - 0.5) * strength * 0.04;

  vec4 texel = texture(DiffuseSampler, newTexCoords);
  vec3 newColor = texel.rgb;

  float blurChance = strength * 0.01;

	for (float f = -strength * 40.0; f < strength * 40.0 + 0.5; f += 1.0) {
		if (f != 0.0) {
      vec2 bTexCoords = vec2(newTexCoords.s, newTexCoords.t + f * pixelSize.y);

			if (bTexCoords.t > 0.0 && bTexCoords.t < 1.0) {
				float randomOne = randomFromVec(vec2(bTexCoords.s, bTexCoords.t + seed));

				if (randomOne < blurChance) {
					newColor = mix(newColor, texture(DiffuseSampler, bTexCoords).rgb, 1.0 / (f * f * 0.004 + 1.0));
				}
			}
		}
	}

  fragColor = vec4(mix(texel.rgb, newColor, totalAlpha), texel.a);
}
