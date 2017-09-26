#version 450 core
uniform sampler2D camColor;
uniform sampler2D camPosition;
uniform sampler2D lightColor;
uniform sampler2D lightPosition;
uniform sampler2D lightDepth;
uniform sampler2D camNormal;
uniform sampler2D lightNormal;

uniform int lightRes_x;
uniform int lightRes_y;

uniform mat4 lightVP;
uniform vec3 lightPosUniform;

const float minDistThreshold=0.001;

in vec2 Location;

out vec4 color;

void main()
{
	vec3 colorseteBase=texture(camColor,Location.xy).xyz;
	vec3 colorsete=colorseteBase;
	//vec3 colorseteLight=texture(lightColor,Location.xy).xyz;
	vec3 depthLight=texture(lightDepth,Location.xy).xyz;
	vec3 normalCam=(texture(lightNormal,Location.xy).xyz*2) - vec3(1,1,1);
	
	vec3 locationWorld=texture(camPosition,Location.xy).xyz;
	
	vec4 locationShadow=lightVP*vec4(locationWorld,1);
	
	//SHADOWMAP
	locationShadow=locationShadow/locationShadow.w;
	float shadowz=texture(lightDepth,(locationShadow.xy+vec2(1,1))/2).z;
	float attenuation=1;
	float threshold=0.001;
	if(shadowz+threshold < (locationShadow.z+1)/2) {
		attenuation=0;
	}
	
	//SHADING
	vec3 lightIncidenceVector=normalize(lightPosUniform-locationWorld);
	float distLightWorld=distance(lightPosUniform,locationWorld);
	float cosLightNormal=max(0,dot(lightIncidenceVector,normalCam));
	colorsete=cosLightNormal*colorsete*attenuation / (distLightWorld*distLightWorld);
	
	//INDIRECT LIGHT
	vec3 indirectLight=vec3(0,0,0);
	for(float w=0.5;w<lightRes_x;w++)
	{
		for(float h=0.5;h<lightRes_y;h++)
		{
			vec3 colorseteLight=texture(lightColor,vec2(w/lightRes_x,h/lightRes_y)).xyz;
			vec3 normalLight=(texture(camNormal,vec2(w/lightRes_x,h/lightRes_y)).xyz*2) - vec3(1,1,1);
			
			vec3 posLight=texture(lightPosition,vec2(w/lightRes_x,h/lightRes_y)).xyz;
			vec3 vecLightWorld=normalize(posLight-locationWorld);
			
			float dist=distance(locationWorld,posLight);
			if(dist<minDistThreshold)dist=minDistThreshold;
			
			indirectLight+=(colorseteLight*max(0,dot(-vecLightWorld,normalLight))*max(0,dot(vecLightWorld,normalCam))/(dist*dist));
		}
	}
	indirectLight=indirectLight*colorseteBase/(lightRes_x*lightRes_y);
	
	colorsete+=indirectLight;
	//Correction because fall-off
	colorsete=pow(colorsete * 10, vec3(1/2.2));
	
	color=vec4(colorsete.xyz,1);
	
	//color=vec4(locationWorld.xyz,1);
}