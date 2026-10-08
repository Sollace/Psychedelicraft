#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D BitsSampler;

#ifdef HAS_CUTOUT
uniform sampler2D CutoutSampler;
in vec2 texCoord;
#endif

in vec4 texProj0;
in vec4 vertexColor;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;

out vec4 fragColor;

void main() {
  vec4 color = vec4(textureProj(BitsSampler, texProj0).rgb, vertexColor.a);

  #ifdef HAS_CUTOUT
    vec4 cutoutColor = texture(CutoutSampler, texCoord);
    color = vec4(mix(color.rgb, cutoutColor.rgb, cutoutColor.a), cutoutColor.a);
  #endif

  fragColor = apply_fog(color,
    sphericalVertexDistance,
    cylindricalVertexDistance,
    FogEnvironmentalStart, FogEnvironmentalEnd,
    FogRenderDistanceStart, FogRenderDistanceEnd,
    FogColor
  );
}
