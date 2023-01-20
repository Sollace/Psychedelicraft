#version 120

vec3 getRotatedColor(vec3 color, float rot)
{
	vec3 returnColor = vec3(0);
	
	for(int i = 0; i < 3; i++)
	{
        float colorAffected = mod(float(i) + rot * 3.0, 3.0);
        
        int col1 = int(floor(colorAffected));
        
        returnColor[col1] += color[i] * (1.0 - (colorAffected - float(col1)));
        returnColor[int(mod(float(col1 + 1), 3.0))] += color[i] * (colorAffected - float(col1));
	}
    
	return returnColor;
}

vec3 getIntensifiedColor(vec3 color)
{
    float s = 2.0;
    float cR = 0.3086;
    float cG = 0.6084;
    float cB = 0.0820;
    
    float rr = (1.0 - s) * cR + s;
    float rg = (1.0 - s) * cG;
    float rb = (1.0 - s) * cB;
    float gr = (1.0 - s) * cR;
    float gg = (1.0 - s) * cG + s;
    float gb = (1.0 - s) * cB;
    float br = (1.0 - s) * cR;
    float bg = (1.0 - s) * cG;
    float bb = (1.0 - s) * cB + s;
    
    vec3 rVec = vec3(color.r * rr + color.g * rg + color.b * rb, color.r * gr + color.g * gg + color.b * gb, color.r * br + color.g * bg + color.b * bb);
    
    return clamp(rVec * rVec * 10.0, 0.0, 1.0);
}

float getBrightness(vec3 color)
{
    float cR = 0.3086;
    float cG = 0.6084;
    float cB = 0.0820;

    return (color.r * cR + color.g * cG + color.b * cB);
}

vec3 getDesaturatedColor(vec3 color)
{
    return vec3(getBrightness(color));
}

float linearize(float value, float zNear, float zFar)
{
    return (2 * zNear) / (zFar + zNear - value * (zFar - zNear));
}

float delinearize(float value, float zNear, float zFar)
{
    return ((zFar + zNear) - ((2.0 * zNear) / value)) / (zFar - zNear);
}

float randomFromSeed(float aSeed)
{
	return fract(mod(aSeed * 12374.123814, 18034.805912));
}

float randomFromVec(vec2 aVec)
{
    return fract(sin(dot(aVec.xy, vec2(12.9898,78.233))) * 43758.5453);
}

vec2 pixelate(vec2 uv, vec2 newRes)
{
    vec2 coord = vec2(ceil(uv.x * newRes.x) / newRes.x, ceil(uv.y * newRes.y) / newRes.y );
    return coord;
}

vec4 reducePalette(vec4 color, float maxCol)
{
    return ceil(color * maxCol - 0.5) / maxCol;
}

