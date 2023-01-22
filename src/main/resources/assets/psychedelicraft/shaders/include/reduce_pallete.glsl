#version 150

vec4 reducePalette(vec4 color, float maxCol) {
    return ceil(color * maxCol - 0.5) / maxCol;
}

