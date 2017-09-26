#version 450 core
uniform vec3 colorUniform;

in vec3 Location;
in vec3 Normal;

layout(location = 0) out vec4 color;
layout(location = 1) out vec3 worldPosition;
layout(location = 2) out vec3 normalTex;

void main()
{
	color=vec4(colorUniform.xyz,1);
	worldPosition=Location;
	normalTex=(Normal+vec3(1,1,1))/2;
}