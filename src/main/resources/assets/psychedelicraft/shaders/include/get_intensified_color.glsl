#version 150

vec3 getIntensifiedColor(vec3 color) {
    float s = 2.0;
    float cR = 0.3086;
    float cG = 0.6084;
    float cB = 0.0820;

    float rr = (1.0 - s) * cR + s;
    float rg = (1.0 - s) * cG;
    float rb = (1.0 - s) * cB;
    float gr = (1.0 - s) * cR;
    float gg = (1.0 - s) * cG + s;
    float gb = (1.0 - s) * cB;
    float br = (1.0 - s) * cR;
    float bg = (1.0 - s) * cG;
    float bb = (1.0 - s) * cB + s;

    vec3 rVec = vec3(color.r * rr + color.g * rg + color.b * rb, color.r * gr + color.g * gg + color.b * gb, color.r * br + color.g * bg + color.b * bb);

    return clamp(rVec * rVec * 10.0, 0.0, 1.0);
}
