#version 120

uniform sampler2D tex0;

uniform vec2 pixelSize;
uniform int vertical;
uniform float totalAlpha;

void main()
{
    gl_FragColor = texture2D(tex0, gl_TexCoord[0].st);
    vec4 newColor = gl_FragColor * 0.2;

    float xMul = (vertical == 0) ? pixelSize.x : 0.0;
    float yMul = (vertical == 1) ? pixelSize.y : 0.0;
    
    for(float i = -1.0; i < 2.0; i += 2.0)
    {
        newColor += texture2D(tex0, clamp(vec2(gl_TexCoord[0].s + 1.0 * i * xMul, gl_TexCoord[0].t + 1.0 * i * yMul), 0.0, 1.0)) * 0.15;
        newColor += texture2D(tex0, clamp(vec2(gl_TexCoord[0].s + 2.0 * i * xMul, gl_TexCoord[0].t + 2.0 * i * yMul), 0.0, 1.0)) * 0.11;
        newColor += texture2D(tex0, clamp(vec2(gl_TexCoord[0].s + 3.0 * i * xMul, gl_TexCoord[0].t + 3.0 * i * yMul), 0.0, 1.0)) * 0.09;
        newColor += texture2D(tex0, clamp(vec2(gl_TexCoord[0].s + 4.0 * i * xMul, gl_TexCoord[0].t + 4.0 * i * yMul), 0.0, 1.0)) * 0.05;
    }
    
    gl_FragColor = mix(gl_FragColor, newColor, totalAlpha);
}
