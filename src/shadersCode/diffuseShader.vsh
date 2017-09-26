#version 450 core
							
uniform mat4 viewProjectionMatrix; //The already multiplied view and projection matrix


in vec3 location;
in vec3 normal;

out vec3 Location;
out vec3 Normal;

void main()
{
	Location=location;
	Normal=normal;
	
	gl_Position=viewProjectionMatrix * vec4(location.xyz,1);
}