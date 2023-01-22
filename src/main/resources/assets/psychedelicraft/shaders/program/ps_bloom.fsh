#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform vec2 pixelSize;
uniform float vertical;
uniform float totalAlpha;

out vec4 fragColor;

void main() {
    vec4 texel = texture(DiffuseSampler, texCoord);
    vec3 newColor = texel.rgb;
    vec3 bloomColor = vec3(0.0);

    vec2 dirVec = vec2((vertical == 0) ? pixelSize.x : 0.0, (vertical == 1) ? pixelSize.y : 0.0) * 3.0;

    float colorInfluence = 15.0 / (5.0 + newColor.r + newColor.g + newColor.b);
    colorInfluence = colorInfluence * colorInfluence;

    for (float i = -1.0; i < 2.0; i += 2.0) {
        vec2 activeDirVec = i * dirVec;
        bloomColor += texture(DiffuseSampler, clamp(texCoord + 1.0 * activeDirVec, 0.0, 1.0)).rgb * 0.028 * colorInfluence;
        bloomColor += texture(DiffuseSampler, clamp(texCoord + 2.0 * activeDirVec, 0.0, 1.0)).rgb * 0.020 * colorInfluence;
        bloomColor += texture(DiffuseSampler, clamp(texCoord + 3.0 * activeDirVec, 0.0, 1.0)).rgb * 0.016 * colorInfluence;
        bloomColor += texture(DiffuseSampler, clamp(texCoord + 4.0 * activeDirVec, 0.0, 1.0)).rgb * 0.012 * colorInfluence;
    }

    newColor += bloomColor * bloomColor * bloomColor;

    fragColor = vec4(mix(texel.rgb, newColor, totalAlpha), 1.0);
}
