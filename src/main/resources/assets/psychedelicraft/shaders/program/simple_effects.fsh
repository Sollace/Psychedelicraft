#version 150
#moj_import <psychedelicraft:utils.glsl>

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform float ticks;

uniform float slowColorRotation;
uniform float quickColorRotation;
uniform float colorIntensification;
uniform float desaturation;

out vec4 fragColor;

void main() {
  vec4 texel = texture(DiffuseSampler, texCoord);
  vec3 outcolor = texel.rgb;

  if (slowColorRotation > 0.0) {
    outcolor = mix(outcolor, getRotatedColor(outcolor, mod(ticks, 300.0) / 300.0), slowColorRotation / 2.0);
  }

  if (quickColorRotation > 0.0) {
     outcolor = mix(outcolor, getRotatedColor(outcolor, mod(ticks/* + fogFragCoord[0]*/, 50.0) / 50.0), clamp(quickColorRotation * 1.5, 0.0, 1.0));
  }

  if (colorIntensification != 0.0) {
    outcolor = mix(outcolor, getIntensifiedColor(outcolor), colorIntensification);
  }

  if (desaturation != 0.0) {
    outcolor = mix(outcolor, getDesaturatedColor(outcolor), desaturation);
  }

  fragColor = vec4(outcolor, 1.0);
}
