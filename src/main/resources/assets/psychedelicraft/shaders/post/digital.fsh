#version 150
#moj_import <psychedelicraft:random_from_vec.glsl>
#moj_import <psychedelicraft:get_desaturated_color.glsl>
#moj_import <psychedelicraft:reduce_pallete.glsl>
#moj_import <psychedelicraft:pixelate.glsl>
#moj_import <psychedelicraft:linear.glsl>

float getPixelDensity(vec2 newUV, vec4 newColor) {
    return 1.0 - getBrightness(newColor.rgb);
}

#moj_import <psychedelicraft:apply_digitize.glsl>

void main() {
  apply_digitize();
}
