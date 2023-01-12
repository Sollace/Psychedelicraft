#version 120

uniform sampler2D tex0;

uniform float distance;
uniform float stretch;
uniform float totalAlpha;

void main()
{
    gl_FragColor = texture2D(tex0, gl_TexCoord[0].st);
    vec4 newColor = gl_FragColor * 0.35;

    newColor += texture2D(tex0, vec2(0.5 + (gl_TexCoord[0].s - 0.5) / stretch + distance, gl_TexCoord[0].t)) * 0.325;
    newColor += texture2D(tex0, vec2(0.5 + (gl_TexCoord[0].s - 0.5) / stretch - distance, gl_TexCoord[0].t)) * 0.325;
    
    gl_FragColor = mix(gl_FragColor, newColor, totalAlpha);
}
