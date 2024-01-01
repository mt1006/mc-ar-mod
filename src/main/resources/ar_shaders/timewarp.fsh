/*
Sources:
- https://drive.google.com/file/d/1gjVWNzHF3Gddxb3fUNgEXU38ETuLfVM2/view (https://www.youtube.com/@comradestinger)
*/
#version 330 core

in vec2 texCoord;
out vec4 fragColor;
uniform sampler2D colorTexture;
uniform mat4 viewMatrix;
uniform float heightMul; // 2.0 / projectionMatrix[1][1]
uniform vec3 topLeft;
uniform vec3 topRight;
uniform vec3 bottomLeft;
uniform vec3 bottomRight;
uniform vec2 screenSize;
uniform float farClip;

vec2 worldToScreenPos(vec3 pos)
{
	vec3 finalPos = normalize(pos) * farClip;
	vec3 toCam = (viewMatrix * vec4(finalPos, 1.0)).xyz;
	float height = toCam.z * heightMul;
	float width = screenSize.x / screenSize.y * height;
	vec2 uv = vec2((toCam.x + width / 2) / width, (toCam.y + height / 2) / height);
	return 1.0 - uv;
}

void main()
{
	vec3 pointVector = mix(mix(topLeft, topRight, texCoord.x), mix(bottomLeft, bottomRight, texCoord.x), 1.0 - texCoord.y);
	vec2 screenPos = worldToScreenPos(pointVector);
	fragColor = vec4(texture(colorTexture, screenPos).rgb, 1.0);
}