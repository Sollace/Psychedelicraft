#version 120

#define MAX_LIGHTS 3

uniform sampler2D texture;
uniform sampler2D lightmapTex;

uniform sampler2D texFractal0;
varying vec2 texFractal0Coords;

const int GL_LINEAR = 9729;
const int GL_EXP = 2048;

uniform int uses2DShaders;
uniform float ticks;
uniform int worldTime;
uniform vec2 pixelSize;

uniform int fogMode;
uniform int fogEnabled;
uniform int lightmapEnabled;
uniform int texture2DEnabled;
uniform vec4 overrideColor;
uniform int useScreenTexCoords;

uniform int fractal0TexIndex;

uniform vec4 pulses;
uniform float quickColorRotation;
uniform float slowColorRotation;
uniform float surfaceFractal;
uniform vec4 worldColorization;

uniform float desaturation;
uniform float colorIntensification;

uniform vec3 playerPos;

varying vec3 relativeVertex;
varying vec3 normalVector;
varying vec4 projGLPos;

uniform int glLightEnabled;
uniform float glLightAmbient;
uniform vec3 glLightPos0;
uniform vec2 glLightStrength0;
uniform vec3 glLightPos1;
uniform vec2 glLightStrength1;

uniform float depthMultiplier;

uniform int doShadows;
uniform sampler2D texShadow;
uniform mat4 inverseViewMatrix;
uniform mat4 sunMatrix;
uniform vec2 sunDepthRange;
uniform float shadowBias;

uniform int colorSafeMode;

void main()
{
    if (texture2DEnabled == 1)
    {
        if (useScreenTexCoords == 1)
            gl_FragColor = texture2D(texture, (gl_FragCoord.xy + gl_TexCoord[0].st) * pixelSize);
        else
            gl_FragColor = texture2D(texture, gl_TexCoord[0].st);
    }
    else
    {
        gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
    }
    
    gl_FragColor *= gl_Color;
    gl_FragColor *= overrideColor;
    
    if (lightmapEnabled == 1)
    {
        float projectedVisibility = 1.0;
        if (doShadows == 1)
        {
            vec2 screenSize = 1.0 / pixelSize;
            vec4 worldCoords = inverseViewMatrix * projGLPos;
            
            vec4 shadowCoord = sunMatrix * worldCoords;
            vec3 ndc_shadowCoord = shadowCoord.xyz / shadowCoord.w;
            vec2 shadowTexCoord = (ndc_shadowCoord.xy + 1.0) * 0.5;
            
            if (shadowTexCoord.x <= 1.0 && shadowTexCoord.x >= 0.0 && shadowTexCoord.y <= 1.0 && shadowTexCoord.y >= 0.0)
            {
                float shadowDepth = texture2D(texShadow, shadowTexCoord).z;
                float calcShadowDepth = (ndc_shadowCoord.z + 1.0) * 0.5;
                
                if (linearize(calcShadowDepth, sunDepthRange.x, sunDepthRange.y) > linearize(shadowDepth, sunDepthRange.x, sunDepthRange.y) + shadowBias)
                {
                    projectedVisibility = 0.5;
                }
            }
        }

        vec4 lighting = texture2D(lightmapTex, gl_TexCoord[1].st);
        gl_FragColor.rgb *= min(lighting.rgb, vec3(projectedVisibility));
    }
    
    if (glLightEnabled == 1)
    {
        vec3 finalLightColor = vec3(glLightAmbient);
        
        for (int i = 0; i < MAX_LIGHTS; i++)
        {
            float glLightColor = glLightAmbient;
            vec3 lightVec = normalize(gl_LightSource[i].position.xyz);
            vec4 lightDiff = gl_FrontLightProduct[i].diffuse * max(dot(normalVector, lightVec), 0.0);
            
            finalLightColor += lightDiff.rgb;
        }

        gl_FragColor.rgb = gl_FragColor.rgb * clamp(finalLightColor, 0.0, 1.0);
    }
    
    gl_FragColor = clamp(gl_FragColor, 0.0, 1.0);

    if (fogEnabled == 1)
    {
        if (fogMode == GL_EXP)
            gl_FragColor.rgb = mix(gl_FragColor.rgb, gl_Fog.color.rgb, 1.0 - clamp(exp(-gl_Fog.density * gl_FogFragCoord), 0.0, 1.0));
        else if (fogMode == GL_LINEAR)
            gl_FragColor.rgb = mix(gl_FragColor.rgb, gl_Fog.color.rgb, clamp((gl_FogFragCoord - gl_Fog.start) * gl_Fog.scale, 0.0, 1.0));
    }

    if(surfaceFractal > 0.0)
    {
        vec4 fractalColor = texture2D(texFractal0, texFractal0Coords);
        float avg = (fractalColor.r + fractalColor.g + fractalColor.b) / 3.0;
        gl_FragColor.rgb = mix(gl_FragColor.rgb, getRotatedColor(gl_FragColor.rgb, avg * surfaceFractal), surfaceFractal);
    }

    if(colorSafeMode == 0 && pulses.a > 0.0)
    {
        float pulseA = (sin((gl_FogFragCoord - ticks) / 5.0) - 0.4) * pulses.a;
        if (pulseA > 0.0)
            gl_FragColor.rgb = mix(gl_FragColor.rgb, (gl_FragColor.rgb + 1.0) * pulses.rgb, pulseA);
    }

    if (uses2DShaders == 0)
    {
        if(slowColorRotation > 0.0)
            gl_FragColor.rgb = mix(gl_FragColor.rgb, getRotatedColor(gl_FragColor.rgb, mod(ticks, 300.0) / 300.0), slowColorRotation);

        if(quickColorRotation > 0.0)
            gl_FragColor.rgb = mix(gl_FragColor.rgb, getRotatedColor(gl_FragColor.rgb, mod(ticks + gl_FogFragCoord, 50.0) / 50.0), quickColorRotation);

        if (colorIntensification != 0.0)
            gl_FragColor.rgb = mix(gl_FragColor.rgb, getIntensifiedColor(gl_FragColor.rgb), colorIntensification);

        if (desaturation != 0.0)
            gl_FragColor.rgb = mix(gl_FragColor.rgb, getDesaturatedColor(gl_FragColor.rgb), desaturation);    
    }

    if(worldColorization.a > 0.0)
    {
        vec3 c1 = gl_FragColor.rgb;
        vec3 c2 = worldColorization.rgb;
        
        float distR = sqrt((c1.r - c2.r) * (c1.r - c2.r));
        float distG = sqrt((c1.g - c2.g) * (c1.g - c2.g));
        float distB = sqrt((c1.b - c2.b) * (c1.b - c2.b));
        
        float dist = clamp((distR + distG + distB), 0.0, 1.0);
        for (int i = 0; i < 4; i++)
            dist *= dist;
        float harmonizeStrength = dist * 3.0;

        float disk = (sin((gl_FogFragCoord - ticks) * 0.1434234) - 0.4) * 0.8;
        harmonizeStrength += disk;

        float disk2 = (sin((gl_FogFragCoord - ticks) * -0.12313) - 0.2) * 0.8;
        harmonizeStrength += disk2;

        float disk3 = sin((gl_FogFragCoord - ticks) * -0.051233) * 0.2 * sin(ticks * 0.1321334);
        harmonizeStrength += disk3;

        harmonizeStrength = clamp(harmonizeStrength, 0.0, 3.0);
        
        vec3 harmonizedColor;

        if (harmonizeStrength < 1.0)
            harmonizedColor = mix(worldColorization.rgb, vec3(0.5), harmonizeStrength); // Max of 1.0
        else
            harmonizedColor = mix(vec3(0.5), vec3(1.0) - worldColorization.rgb, (harmonizeStrength - 1.0) * 0.5); // Max of 2.0

        if (colorSafeMode != 0) // Make sure we don't add brightness
            harmonizedColor *= gl_FragColor.rgb;

        gl_FragColor.rgb = mix(gl_FragColor.rgb, harmonizedColor, worldColorization.a);
    }

    gl_FragColor = clamp(gl_FragColor, 0.0, 1.0);
    gl_FragDepth = gl_FragCoord.z * depthMultiplier;
}
