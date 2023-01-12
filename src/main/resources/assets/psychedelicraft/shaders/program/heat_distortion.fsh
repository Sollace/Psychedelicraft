#version 120

uniform sampler2D tex0;
uniform sampler2D tex1;
uniform sampler2D tex2;
uniform sampler2D noiseTex;

uniform float totalAlpha;

uniform float ticks;
uniform float strength;

void main()
{
	vec4 depthPixel = texture2D(tex2, gl_TexCoord[0].st);
	vec4 noisePixel1 = texture2D(noiseTex, gl_TexCoord[0].st * 4.0 + vec2(ticks * 0.324823048, ticks * 0.48913801));
	vec4 noisePixel2 = texture2D(noiseTex, gl_TexCoord[0].ts * 4.0 + vec2(ticks * 0.52890348, ticks * 0.6318212));
	vec4 joinedNoise = noisePixel1 + noisePixel2 - 1.0;
	float depthMul = min(0.4 / sqrt(sqrt(sqrt(1.0 - depthPixel.r))) - 0.4, 1.0);
	vec4 newColor = texture2D(tex0, clamp(gl_TexCoord[0].st + joinedNoise.rg * strength * depthMul, 0.0, 1.0));

	if (totalAlpha == 1.0)
		gl_FragColor = newColor;
	else
		gl_FragColor = mix(texture2D(tex0, gl_TexCoord[0].st), newColor, totalAlpha);
}
