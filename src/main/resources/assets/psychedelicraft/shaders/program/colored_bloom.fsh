#version 120

uniform sampler2D tex0;

uniform vec2 pixelSize;
uniform int vertical;
uniform float totalAlpha;
uniform vec3 bloomColor;

float influenceFromColor(vec3 color1, vec3 color2)
{
    vec3 rdistCol = (color1 - color2);
    vec3 distCol = sqrt(rdistCol * rdistCol);
    
    float influence = 1.0 - (distCol.r + distCol.g + distCol.b) * 2.0;
    
    return clamp(influence, 0.0, 1.0);
}

void main()
{
    gl_FragColor = texture2D(tex0, gl_TexCoord[0].st);
    vec4 newColor = gl_FragColor;
    float bloomInfluence = 0.0;

    vec2 dirVec = vec2((vertical == 0) ? pixelSize.x : 0.0, (vertical == 1) ? pixelSize.y : 0.0);

    for(float i = -1.0; i < 2.0; i += 2.0)
    {
        vec2 activeDirVec = i * dirVec;
        vec3 color1 = texture2D(tex0, clamp(gl_TexCoord[0].st + 1.0 * activeDirVec, 0.0, 1.0)).rgb;
        vec3 color2 = texture2D(tex0, clamp(gl_TexCoord[0].st + 2.0 * activeDirVec, 0.0, 1.0)).rgb;
        vec3 color3 = texture2D(tex0, clamp(gl_TexCoord[0].st + 3.0 * activeDirVec, 0.0, 1.0)).rgb;
        vec3 color4 = texture2D(tex0, clamp(gl_TexCoord[0].st + 4.0 * activeDirVec, 0.0, 1.0)).rgb;
        
        bloomInfluence += influenceFromColor(color1, bloomColor) * 0.028 * 2.0;
        bloomInfluence += influenceFromColor(color2, bloomColor) * 0.020 * 2.0;
        bloomInfluence += influenceFromColor(color3, bloomColor) * 0.016 * 2.0;
        bloomInfluence += influenceFromColor(color4, bloomColor) * 0.012 * 2.0;
    }
    
    newColor.rgb = mix(newColor.rgb, bloomColor, clamp(bloomInfluence, 0.0, 1.0));
        
    gl_FragColor = mix(gl_FragColor, newColor, totalAlpha);
}
