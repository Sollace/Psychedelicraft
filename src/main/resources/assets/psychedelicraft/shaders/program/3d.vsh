#version 120

uniform float ticks;
uniform int worldTime;

uniform int fogMode;
uniform int lightmapEnabled;
uniform int texture2DEnabled;
uniform vec4 overrideColor;

uniform vec4 fractal0TexCoords;
varying vec2 texFractal0Coords;

uniform float bigWaves;
uniform float smallWaves;
uniform float wiggleWaves;
uniform float distantWorldDeformation;
uniform float surfaceFractal;

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

void main()
{    
	gl_Position = ftransform();
    projGLPos = vec4(gl_Position);
    relativeVertex = vec3(gl_ModelViewMatrix * gl_Vertex);
    normalVector = normalize(gl_NormalMatrix * gl_Normal);

    gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_TextureMatrix[1] * gl_MultiTexCoord1;
    
	if (surfaceFractal > 0.0)
    {
        texFractal0Coords = vec2(mix(fractal0TexCoords[0], fractal0TexCoords[2], (mod(gl_Vertex[0] + gl_Vertex[1], 4.0)) / 4.0), mix(fractal0TexCoords[1], fractal0TexCoords[3], (mod(gl_Vertex[2] + gl_Vertex[1], 4.0)) / 4.0));
    }

    gl_FrontColor = gl_Color;
        
    vec3 vVertex = vec3(gl_ModelViewMatrix * gl_Vertex);
	gl_FogFragCoord = length(vVertex);
    
    if (smallWaves > 0.0)
    {        
        float w1 = 8.0;
        
        gl_Position[1] += sin((gl_Vertex[0] + ticks / 5.0) / w1 * 3.14159 * 2.0) * sin((gl_Vertex[2] + ticks / 5.0) / w1 * 3.14159 * 2.0) * smallWaves * 1.5;
        gl_Position[1] -= sin((playerPos.x + ticks / 5.0) / w1 * 3.14159 * 2.0) * sin((playerPos.z + ticks / 5.0) / w1 * 3.14159 * 2.0) * smallWaves * 1.5;

        float w2 = 16.0;

        gl_Position[1] += sin((gl_Vertex[0] + ticks / 8.0) / w2 * 3.14159 * 2.0) * sin((gl_Vertex[2]) / w2 * 3.14159 * 2.0) * smallWaves * 3.0;
        gl_Position[1] -= sin((playerPos.x + ticks / 8.0) / w2 * 3.14159 * 2.0) * sin((playerPos.z) / w2 * 3.14159 * 2.0) * smallWaves * 3.0;
        
        gl_Position[0] = mix(gl_Position[0], gl_Position[0] * (1.0 + gl_FogFragCoord / 20.0), smallWaves);
        gl_Position[1] = mix(gl_Position[1], gl_Position[1] * (1.0 + gl_FogFragCoord / 20.0), smallWaves);
    }

    if(wiggleWaves > 0.0)
    {
        float w1 = 8.0;

        gl_Position[0] += sin((gl_Vertex[1] + ticks / 8.0) / w1 * 3.14159 * 2.0) * sin((gl_Vertex[2] + ticks / 5.0) / w1 * 3.14159 * 2.0) * wiggleWaves;
    }

    if(distantWorldDeformation > 0.0 && gl_FogFragCoord > 5.0)
        gl_Position[1] += (sin(gl_FogFragCoord / 8.0 * 3.14159 * 2.0) + 1.0) * distantWorldDeformation * (gl_FogFragCoord - 5.0) / 8.0;
    
    if(bigWaves > 0.0)
    {
        if (gl_Position[2] > 0.1)
        {
            float dDist = (gl_Position[2] - 0.1) * bigWaves;
            if (gl_Position[2] > 20.0)
            {
                dDist = (20.0 - 0.1) * bigWaves + (gl_Position[2] - 20.0) * bigWaves * 0.3;
            }

            float inf1 = sin(ticks * 0.0086465563) * dDist;
            float inf2 = cos(ticks * 0.0086465563) * dDist;
            float inf3 = sin(ticks * 0.0091033941) * dDist;
            float inf4 = cos(ticks * 0.0091033941) * dDist;
            float inf5 = sin(ticks * 0.0064566190) * dDist;
            float inf6 = cos(ticks * 0.0064566190) * dDist;

            float pMul = 1.3;
            
            gl_Position[0] += sin(gl_Position[2] * 0.1 * sin(ticks * 0.001849328) + ticks * 0.014123412) * 0.5 * inf1 * pMul;
            gl_Position[1] += cos(gl_Position[2] * 0.1 * sin(ticks * 0.001234728) + ticks * 0.017481893) * 0.4 * inf1 * pMul;
            
            gl_Position[0] += sin(gl_Position[1] * 0.1 * sin(ticks * 0.001523784) + ticks * 0.021823911) * 0.2 * inf2 * pMul;
            gl_Position[1] += sin(gl_Position[0] * 0.1 * sin(ticks * 0.001472387) + ticks * 0.023193141) * 0.08 * inf2 * pMul;

            gl_Position[0] += sin(gl_Position[2] * 0.15 * sin(ticks * 0.001284923) + ticks * 0.019404289) * 0.25 * inf3 * pMul;
            gl_Position[1] += cos(gl_Position[2] * 0.15 * sin(ticks * 0.001482938) + ticks * 0.018491238) * 0.15 * inf3 * pMul;
            
            gl_Position[0] += sin(gl_Position[1] * 0.05 * sin(ticks * 0.001283942) + ticks * 0.012942342) * 0.4 * inf4 * pMul;
            gl_Position[1] += sin(gl_Position[0] * 0.05 * sin(ticks * 0.001829482) + ticks * 0.012981328) * 0.35 * inf4 * pMul;

            gl_Position[2] += sin(gl_Position[1] * 0.13 * sin(ticks * 0.02834472) + ticks * 0.023482934) * 0.1 * inf5 * pMul;
            gl_Position[2] += sin(gl_Position[0] * 0.124 * sin(ticks * 0.00184298) + ticks * 0.018394082) * 0.05 * inf6 * pMul;
            gl_Position[3] += sin(gl_Position[1] * 0.13 * sin(ticks * 0.02834472) + ticks * 0.023482934) * 0.1 * inf5 * pMul;
            gl_Position[3] += sin(gl_Position[0] * 0.124 * sin(ticks * 0.00184298) + ticks * 0.018394082) * 0.05 * inf6 * pMul;
        }
    }
}
