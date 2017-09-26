#version 450 core
uniform sampler2D camColor;
uniform sampler2D camPosition;
uniform sampler2D lightColor;
uniform sampler2D lightPosition;
uniform sampler2D camAlbedo;
uniform sampler2D lightDepth;

uniform int lightRes_x;
uniform int lightRes_y;

uniform mat4 lightVP;

in vec2 Location;

out vec4 color;

void main()
{
	vec3 colorsete=vec3(0,0,0);
	if(Location.x>0){
		if(Location.y>0) colorsete=texture(camColor,vec2(Location.x,Location.y)).xyz;
		else colorsete=texture(camPosition,vec2(Location.x,Location.y)).xyz;
	}
	else{
		if(Location.y>0) colorsete=texture(lightColor,vec2(Location.x,Location.y)).xyz;
		else colorsete=texture(lightDepth,vec2(Location.x,Location.y)).xyz;
	}
	vec3 accumIndirectLight=vec3(0,0,0);
	/*for(int w=0;w<lightRes_x;w++)
	{
		for(int h=0;h<lightRes_y;h++)
		{
			accumIndirectLight+=texture(lightColor,vec2(float(w)/lightRes_x,float(h)/lightRes_y)).xyz;
		}
	}*/
	
	//colorsete=texture(camColor,vec2(Location.x,Location.y)).xyz + accumIndirectLight/10000;
	vec4 posLight=lightVP*vec4(texture(camPosition,vec2(Location.x,Location.y)).xyz,1);
	bool visible=posLight.z<=texture(lightDepth,(vec2(posLight.x,posLight.y)+vec2(1,1))/2).z;
	float shadowAttenuation=visible?1:0;
	
	if(Location.x>0){
	color=vec4(texture(lightDepth,vec2(posLight.x,posLight.y)).xyz,1);///*vec4(posLight.zzz/10,1);//*/vec4(colorsete.xyz/*shadowAttenuation*/,1);
	}
	else{
	color=vec4(texture(lightDepth,vec2(Location.x,Location.y)).xyz,1);
	}
}