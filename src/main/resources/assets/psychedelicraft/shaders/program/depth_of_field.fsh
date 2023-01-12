#version 120

uniform sampler2D tex;
uniform sampler2D depthTex;

uniform vec2 pixelSize;
uniform int vertical;

uniform float focalPointNear;
uniform float focalBlurNear;
uniform float focalPointFar;
uniform float focalBlurFar;

uniform vec2 depthRange;

float getLinearDepth(vec2 newUV)
{
    float depth = texture2D(depthTex, newUV).r;
    return linearize(depth, depthRange.x, depthRange.y);
}

void main()
{
    gl_FragColor = texture2D(tex, gl_TexCoord[0].st);
    vec4 newColor = gl_FragColor * 0.2;

    float depth = getLinearDepth(gl_TexCoord[0].st);
    float focalDepth = 0.0;
    if (depth < focalPointNear)
        focalDepth = (focalPointNear - depth) / focalPointNear * focalBlurNear;
    else if (depth > focalPointFar)
        focalDepth = (depth - focalPointFar) / focalPointFar * focalBlurFar;

    focalDepth = min(focalDepth, 1.0);

    if (focalDepth > 0.0)
    {
        float xMul = (vertical == 0) ? pixelSize.x : 0.0;
        float yMul = (vertical == 1) ? pixelSize.y : 0.0;

        for(float i = -1.0; i < 2.0; i += 2.0)
        {
            newColor += texture2D(tex, vec2(gl_TexCoord[0].s + 1.0 * i * xMul, gl_TexCoord[0].t + 1.0 * i * yMul)) * 0.15;
            newColor += texture2D(tex, vec2(gl_TexCoord[0].s + 2.0 * i * xMul, gl_TexCoord[0].t + 2.0 * i * yMul)) * 0.11;
            newColor += texture2D(tex, vec2(gl_TexCoord[0].s + 3.0 * i * xMul, gl_TexCoord[0].t + 3.0 * i * yMul)) * 0.09;
            newColor += texture2D(tex, vec2(gl_TexCoord[0].s + 4.0 * i * xMul, gl_TexCoord[0].t + 4.0 * i * yMul)) * 0.05;
        }
        
        gl_FragColor = mix(gl_FragColor, newColor, focalDepth);
    }
}
