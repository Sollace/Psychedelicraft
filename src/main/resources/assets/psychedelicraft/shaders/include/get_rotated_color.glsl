#version 150

vec3 getRotatedColor(vec3 color, float rot) {
	vec3 returnColor = vec3(0);

	for (int i = 0; i < 3; i++) {
        float colorAffected = mod(float(i) + rot * 3.0, 3.0);

        int col1 = int(floor(colorAffected));

        returnColor[col1] += color[i] * (1.0 - (colorAffected - float(col1)));
        returnColor[int(mod(float(col1 + 1), 3.0))] += color[i] * (colorAffected - float(col1));
	}

	return returnColor;
}

