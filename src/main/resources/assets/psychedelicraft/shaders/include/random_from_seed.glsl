#version 150

float randomFromSeed(float aSeed) {
	return fract(mod(aSeed * 12374.123814, 18034.805912));
}
