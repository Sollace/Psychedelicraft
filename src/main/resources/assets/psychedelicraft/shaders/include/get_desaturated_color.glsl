#version 150

float getBrightness(vec3 color) {
    float cR = 0.3086;
    float cG = 0.6084;
    float cB = 0.0820;

    return (color.r * cR + color.g * cG + color.b * cB);
}

vec3 getDesaturatedColor(vec3 color) {
    return vec3(getBrightness(color));
}
