#version 330 core

in vec2 texCoord;
out vec4 fragColor;
uniform sampler2D colorTexture;
uniform vec3 color;

void main()
{
	fragColor = texture(colorTexture, texCoord) * vec4(color, 1.0);
}