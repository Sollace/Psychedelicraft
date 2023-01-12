#version 120

uniform sampler2D tex0;

uniform vec2 pixelSize;
uniform float totalAlpha;

void main()
{
    vec2 newCoords = gl_TexCoord[0].st * (1.0 - pixelSize * 8.0) + pixelSize * 4.0;
    vec2 pixelMul = (gl_TexCoord[0].st - 0.5) * 4.0 * pixelSize;

    vec4 newColor = texture2D(tex0, newCoords) * 0.2;

    newColor += texture2D(tex0, clamp(newCoords + pixelMul, 0.0, 1.0)) * 0.3;
    newColor += texture2D(tex0, clamp(newCoords + pixelMul * 2.0, 0.0, 1.0)) * 0.22;
    newColor += texture2D(tex0, clamp(newCoords + pixelMul * 3.0, 0.0, 1.0)) * 0.18;
    newColor += texture2D(tex0, clamp(newCoords + pixelMul * 4.0, 0.0, 1.0)) * 0.1;
    
	if (totalAlpha == 1.0)
		gl_FragColor = newColor;
	else
		gl_FragColor = mix(texture2D(tex0, gl_TexCoord[0].st), newColor, totalAlpha);
}
