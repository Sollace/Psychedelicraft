#version 120

uniform sampler2D tex0;

uniform float ticks;

uniform float slowColorRotation;
uniform float quickColorRotation;
uniform float colorIntensification;
uniform float desaturation;

void main()
{
	gl_FragColor = texture2D(tex0, gl_TexCoord[0].st);

	if(slowColorRotation > 0.0)
		gl_FragColor.rgb = mix(gl_FragColor.rgb, getRotatedColor(gl_FragColor.rgb, mod(ticks, 300.0) / 300.0), slowColorRotation / 2.0);

    if(quickColorRotation > 0.0)
        gl_FragColor.rgb = mix(gl_FragColor.rgb, getRotatedColor(gl_FragColor.rgb, mod(ticks + gl_FogFragCoord, 50.0) / 50.0), clamp(quickColorRotation * 1.5, 0.0, 1.0));

	if (colorIntensification != 0.0)
		gl_FragColor.rgb = mix(gl_FragColor.rgb, getIntensifiedColor(gl_FragColor.rgb), colorIntensification);

	if (desaturation != 0.0)
		gl_FragColor.rgb = mix(gl_FragColor.rgb, getDesaturatedColor(gl_FragColor.rgb), desaturation);
}
