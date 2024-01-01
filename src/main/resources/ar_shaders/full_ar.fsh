/*
Sources:
- https://drive.google.com/file/d/1gjVWNzHF3Gddxb3fUNgEXU38ETuLfVM2/view (https://www.youtube.com/@comradestinger)
- https://stackoverflow.com/a/6657284
*/
#version 330 core

in vec2 texCoord;
out vec4 fragColor;
uniform sampler2D colorTexture;
uniform sampler2D depthTexture;
uniform mat4 viewMatrix;
uniform float heightMul; // 2.0 / projectionMatrix[1][1]
uniform vec3 cameraVector;
uniform vec3 cameraPosition;
uniform vec3 topLeft;
uniform vec3 topRight;
uniform vec3 bottomLeft;
uniform vec3 bottomRight;
uniform vec2 screenSize;
uniform float farClip;
uniform int sequenceN;
uniform float sequenceR;
uniform float sequenceA0;

const float nearClip = 0.05;
float screenRation;
float clipMul, clipDiff;

float linearDepth(float val)
{
	float sampleVal = ((val + 1.0) / 2.0);
	return (clipMul / (farClip - sampleVal * clipDiff)) / 2.0;
}

vec3 getWorldPos(vec3 vector, vec2 uv)
{
	float dotProduct = dot(cameraVector, vector);
	float sceneDistance = linearDepth(texture(depthTexture, uv).r) / dotProduct;
	return vector * sceneDistance;
}

vec2 worldToScreenPos(vec3 vector)
{
	vec3 finalPos = vector * farClip;
	vec3 toCam = (viewMatrix * vec4(finalPos, 1.0)).xyz;
	float height = toCam.z * heightMul;
	float width = screenRation * height;
	vec2 uv = vec2((toCam.x + width / 2.0) / width, (toCam.y + height / 2.0) / height);
	return 1.0 - uv;
}

void main()
{
	screenRation = screenSize.x / screenSize.y;
	clipMul = nearClip * farClip;
	clipDiff = farClip - nearClip;
	vec3 pointVector = mix(mix(topLeft, topRight, texCoord.x), mix(bottomLeft, bottomRight, texCoord.x), 1.0 - texCoord.y);

	vec3 pos = cameraPosition;
	bool occluded = false;
	float sequenceA = sequenceA0;
    for (int i = 0; i < sequenceN; i++)
    {
        pos += pointVector * sequenceA;

		float posLen = length(pos);
        vec3 normalizedPos = pos / posLen;
		vec2 newScreenPos = worldToScreenPos(normalizedPos);
        vec3 tracedPos = getWorldPos(normalizedPos, newScreenPos);
		float distanceDiff = posLen - length(tracedPos);

		if (distanceDiff > sequenceA) { occluded = true; }
		if (distanceDiff > 0) { break; }
		sequenceA *= sequenceR;
	}

	vec2 screenPos = worldToScreenPos(normalize(pos));
	vec3 texColor = texture(colorTexture, screenPos).rgb;
	if (occluded)
	{
		vec2 offset1 = screenPos + vec2(1.0, 0.0) * 0.01;
		vec2 offset2 = screenPos + vec2(0.0, 1.0) * 0.01;
		vec2 offset3 = screenPos + vec2(-1.0, 0.0) * 0.01;
		vec2 offset4 = screenPos + vec2(0.0, -1.0) * 0.01;
		float depth0 = linearDepth(texture(depthTexture, screenPos).r);
		float depth1 = linearDepth(texture(depthTexture, offset1).r);
		float depth2 = linearDepth(texture(depthTexture, offset2).r);
		float depth3 = linearDepth(texture(depthTexture, offset3).r);
		float depth4 = linearDepth(texture(depthTexture, offset4).r);
		float furthest = max(max(max(max(depth0, depth1), depth2), depth3), depth4);
		if (furthest == depth1) { texColor = texture(colorTexture, offset1).rgb; }
		if (furthest == depth2) { texColor = texture(colorTexture, offset2).rgb; }
		if (furthest == depth3) { texColor = texture(colorTexture, offset3).rgb; }
		if (furthest == depth4) { texColor = texture(colorTexture, offset4).rgb; }
	}

	fragColor = vec4(texColor, 1.0);
}