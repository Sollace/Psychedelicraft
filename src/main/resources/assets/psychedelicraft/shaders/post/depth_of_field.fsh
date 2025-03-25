#version 150
#moj_import <psychedelicraft:linear.glsl>

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

in vec2 texCoord;

uniform vec2 pixelSize;
uniform float vertical;
uniform float focalPointNear;
uniform float focalBlurNear;
uniform float focalPointFar;
uniform float focalBlurFar;

uniform vec2 depthRange;

out vec4 fragColor;

float getLinearDepth(vec2 newUV) {
  float depth = texture(DepthSampler, newUV).r;
  return linearize(depth, depthRange.x, depthRange.y);
}

void main() {
  vec4 texel = texture(DiffuseSampler, texCoord);
  vec3 newColor = texel.rgb;

  float depth = getLinearDepth(texCoord);
  float focalDepth = 0.0;
  if (depth < focalPointNear) {
    focalDepth = (focalPointNear - depth) / focalPointNear * focalBlurNear;
  } else if (depth > focalPointFar) {
    focalDepth = (depth - focalPointFar) / focalPointFar * focalBlurFar;
  }

  focalDepth = min(focalDepth, 1.0);

  if (focalDepth > 0.0) {
    float xMul = (vertical == 0) ? pixelSize.x : 0.0;
    float yMul = (vertical == 1) ? pixelSize.y : 0.0;

    for (float i = -1.0; i < 2.0; i += 2.0) {
      newColor = newColor * 0.85 + texture(DiffuseSampler, vec2(texCoord.s + 1.0 * i * xMul, texCoord.t + 1.0 * i * yMul)).rgb * 0.15;
      newColor = newColor * 0.89 + texture(DiffuseSampler, vec2(texCoord.s + 2.0 * i * xMul, texCoord.t + 2.0 * i * yMul)).rgb * 0.11;
      newColor = newColor * 0.91 + texture(DiffuseSampler, vec2(texCoord.s + 3.0 * i * xMul, texCoord.t + 3.0 * i * yMul)).rgb * 0.09;
      newColor = newColor * 0.95 +  texture(DiffuseSampler, vec2(texCoord.s + 4.0 * i * xMul, texCoord.t + 4.0 * i * yMul)).rgb * 0.05;
    }
    
    newColor = mix(texel.rgb, newColor, focalDepth);
  }
  
  fragColor = vec4(newColor, 1);
}
