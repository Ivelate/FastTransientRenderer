#version 450 core

layout(binding=4) uniform sampler2D normTex;
uniform int frameToPlay;
layout(binding=0 , rgba32f) uniform readonly image3D voxelStorage;

layout(location=0) out vec4 color;
in vec2 Location;

void main()
{
	ivec3 position=ivec3(gl_FragCoord.xy,frameToPlay);
	vec3 colorsete=imageLoad(voxelStorage,position).rgb;
	colorsete=pow(colorsete * 2000, vec3(1/2.2));
	color=vec4(colorsete.xyz,1);
	//color=vec4(texture(normTex,Location.xy).xxx*10000,1);
	//color=vec4(position.xyz,1);
}