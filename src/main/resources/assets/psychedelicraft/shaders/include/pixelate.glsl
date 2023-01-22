#version 150

vec2 pixelate(vec2 uv, vec2 newRes) {
    vec2 coord = vec2(ceil(uv.x * newRes.x) / newRes.x, ceil(uv.y * newRes.y) / newRes.y );
    return coord;
}
