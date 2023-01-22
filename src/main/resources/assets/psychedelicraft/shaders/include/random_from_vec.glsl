#version 150

float randomFromVec(vec2 aVec) {
    return fract(sin(dot(aVec.xy, vec2(12.9898,78.233))) * 43758.5453);
}
