#version 450 core	

uniform float scale;
uniform vec2 offset;

in vec2 location;
out vec2 Location;

void main()
{
	vec2 trueLocation=(location.xy+vec2(1,1))/2;
	Location=trueLocation*scale + offset;
	
	gl_Position=vec4(location.xy,0,1);
}