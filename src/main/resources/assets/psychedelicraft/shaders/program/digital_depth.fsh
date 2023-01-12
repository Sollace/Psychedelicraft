#version 120

uniform sampler2D tex;
uniform sampler2D asciiTex;
uniform sampler2D depthTex;

uniform vec2 newResolution;
uniform float textProgress;
uniform float maxColors;
uniform float saturation;
uniform float totalAlpha;

uniform vec2 depthRange;

float getPixelDensity(vec2 newUV, vec4 newColor)
{
    float textureDepth = texture2D(depthTex, newUV).r;

    return linearize(textureDepth, depthRange.x, depthRange.y);
}

void main()
{
    vec2 newUV = (newResolution.x > 0.0 && newResolution.y > 0.0) ? pixelate(gl_TexCoord[0].st, newResolution) : gl_TexCoord[0].st;
    vec4 newColor = texture2D(tex, newUV);
    
    if (saturation < 1.0)
    {
        newColor.rgb = mix(getDesaturatedColor(newColor.rgb), newColor.rgb, saturation);
    }

    if (maxColors >= 0.0)
    {
        newColor = reducePalette(newColor, maxColors);        
    }
    
    if (textProgress > 0.0f)
    {
        float textProg = clamp(textProgress, 0.0, 1.0);
                
        float invTextProg = 1.0 - clamp(0.0, textProg - 0.2, 0.8) * 1.25;
        float bgMaxAlpha = sqrt(invTextProg);
        if ((randomFromVec(newUV) * 0.999) < textProg || bgMaxAlpha < 1.0)
        {
            float binaryProg = textProgress - textProg;

            float pixelDensity = getPixelDensity(newUV, newColor);

            float pixelDensityParts = 95.0;
            float pixelDensityPart = float(ceil(pixelDensity * (pixelDensityParts - 1.0) - (0.5 / pixelDensityParts))) / pixelDensityParts;
            bool isBinary = (randomFromVec(newUV) * 0.999) < binaryProg;
            if (isBinary)
            {
                pixelDensityPart = min((ceil(pixelDensity - 0.25)) / pixelDensityParts, 1.0 / pixelDensityParts);
            }
            
            vec2 innerUV = (newUV - gl_TexCoord[0].st) * newResolution;
            innerUV.x = 1.0 - innerUV.x;
            vec4 textTexturePixel = texture2D(asciiTex, vec2(pixelDensityPart + innerUV.x / pixelDensityParts, innerUV.y * 0.5 + (isBinary ? 0.5 : 0.0)));
            
            newColor.rgb = mix(newColor.rgb, textTexturePixel.rgb * newColor.rgb, clamp(0.0, max(textProg * textProg * textProg, 1.0 - bgMaxAlpha), 1.0));
        }
    }
    
	if (totalAlpha == 1.0)
		gl_FragColor = newColor;
	else
		gl_FragColor = mix(texture2D(tex, gl_TexCoord[0].st), newColor, totalAlpha);
}
