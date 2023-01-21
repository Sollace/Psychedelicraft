#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform vec2 pixelSize;
uniform float vertical;
uniform float totalAlpha;
uniform vec3 bloomColor;

out vec4 fragColor;

float influenceFromColor(vec3 color1, vec3 color2) {
  vec3 rdistCol = (color1 - color2);
  vec3 distCol = sqrt(rdistCol * rdistCol);
  
  float influence = 1.0 - (distCol.r + distCol.g + distCol.b) * 2.0;
  
  return clamp(influence, 0.0, 1.0);
}

void main() {
  vec4 texel = texture(DiffuseSampler, texCoord);
  vec3 newColor = texel.rgb;
  float bloomInfluence = 0.0;

  vec2 dirVec = vec2(
       (vertical == 0) ? pixelSize.x : 0.0,
       (vertical == 1) ? pixelSize.y : 0.0
  );

  for (float i = -1.0; i < 2.0; i += 2.0) {
    vec2 activeDirVec = i * dirVec;
    vec3 color1 = texture(DiffuseSampler, clamp(texCoord + 1.0 * activeDirVec, 0.0, 1.0)).rgb;
    vec3 color2 = texture(DiffuseSampler, clamp(texCoord + 2.0 * activeDirVec, 0.0, 1.0)).rgb;
    vec3 color3 = texture(DiffuseSampler, clamp(texCoord + 3.0 * activeDirVec, 0.0, 1.0)).rgb;
    vec3 color4 = texture(DiffuseSampler, clamp(texCoord + 4.0 * activeDirVec, 0.0, 1.0)).rgb;

    bloomInfluence += influenceFromColor(color1, bloomColor) * 0.028 * 2.0;
    bloomInfluence += influenceFromColor(color2, bloomColor) * 0.020 * 2.0;
    bloomInfluence += influenceFromColor(color3, bloomColor) * 0.016 * 2.0;
    bloomInfluence += influenceFromColor(color4, bloomColor) * 0.012 * 2.0;
  }

  newColor = mix(newColor, bloomColor, clamp(bloomInfluence, 0.0, 1.0));

  fragColor = vec4(mix(texel.rgb, newColor, totalAlpha), 1.0);
}
