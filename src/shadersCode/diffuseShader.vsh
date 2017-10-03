#version 450 core
							
uniform mat4 viewProjectionMatrix; //The already multiplied view and projection matrix

uniform mat4 modelMatrix;


in vec3 location;
in vec3 normal;

out vec3 Location;
out vec3 Normal;

void main()
{
	vec4 locModel=modelMatrix * vec4(location,1);
	Location=locModel.xyz;
	Normal=normal;
	
	gl_Position=viewProjectionMatrix * locModel;
}