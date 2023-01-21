#version 120

uniform sampler2D tex0;

uniform int texture2DEnabled;
uniform int useScreenTexCoords;
uniform vec2 pixelSize;
uniform vec4 overrideColor;

uniform float depthMultiplier;

void main()
{
    if (texture2DEnabled == 1)
    {
        if (useScreenTexCoords == 1)
            gl_FragColor = texture2D(tex0, (gl_FragCoord.xy + gl_TexCoord[0].st) * pixelSize);
        else
            gl_FragColor = texture2D(tex0, gl_TexCoord[0].st);
    }
    else
    {
        gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
    }

    gl_FragColor.a *= gl_Color.a;
    gl_FragColor *= overrideColor;

    if (gl_FragColor.a < 0.2)
        gl_FragColor.a = 0.0; // Additional ALPHA_TEST

    gl_FragDepth = gl_FragCoord.z * depthMultiplier;
    gl_FragColor.rgb = vec3(gl_FragDepth);
}
