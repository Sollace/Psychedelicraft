#version 150

float linearize(float value, float zNear, float zFar) {
    return (2 * zNear) / (zFar + zNear - value * (zFar - zNear));
}

float delinearize(float value, float zNear, float zFar) {
    return ((zFar + zNear) - ((2.0 * zNear) / value)) / (zFar - zNear);
}
