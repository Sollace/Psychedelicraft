#version 120
#moj_import <psychedelicraft:random_from_vec.glsl>
#moj_import <psychedelicraft:get_desaturated_color.glsl>
#moj_import <psychedelicraft:reduce_pallete.glsl>
#moj_import <psychedelicraft:pixelate.glsl>
#moj_import <psychedelicraft:linear.glsl>

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform sampler2D asciiTex;

in vec2 texCoord;

uniform vec2 newResolution;
uniform float textProgress;
uniform float maxColors;
uniform float saturation;
uniform float totalAlpha;

uniform vec2 depthRange;

out vec4 fragColor;

float getPixelDensity(vec2 newUV, vec4 newColor) {
  float textureDepth = texture(DepthSampler, newUV).r;
  return linearize(textureDepth, depthRange.x, depthRange.y);
}

#moj_import <psychedelicraft:apply_digitize.glsl>

void main() {
  apply_digitize();
}
