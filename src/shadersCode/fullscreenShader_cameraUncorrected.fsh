#version 450 core

//Memory access is COHERENT if this shader is the only one that access this X,Y coordinates
layout(binding=0 , rgba32f) uniform image3D voxelStorage;

uniform sampler2D camColor;
uniform sampler2D camPosition;
uniform sampler2D lightColor;
uniform sampler2D lightPosition;
uniform sampler2D lightDepth;
uniform sampler2D camNormal;
uniform sampler2D lightNormal;
uniform sampler2D pxAngles;

uniform int directLight;

uniform int lightRes_x;
uniform int lightRes_y;

uniform mat4 lightVP;
uniform vec3 lightPosUniform;

uniform vec3 camPosUniform;

uniform float t0;
uniform float tdelta;
uniform int tres;

const float minDistThreshold=0.001;

in vec2 Location;

out vec4 color;

void accumRadiance(ivec2 pos,float dist,vec3 addRadiance)
{
	int z=int(floor((dist-t0)/tdelta)); //Can we save the floor?
	vec3 radiance=imageLoad(voxelStorage,ivec3(pos.xy,z)).xyz + addRadiance;
	imageStore(voxelStorage,ivec3(pos.xy,z),vec4(radiance.xyz,1));
}
void main()
{
	ivec2 screenPos=ivec2(gl_FragCoord.xy);
	vec3 colorseteBase=texture(camColor,Location.xy).xyz; //Albedo from the camera
	vec3 colorsete=colorseteBase;

	vec3 normalCam=(texture(lightNormal,Location.xy).xyz*2) - vec3(1,1,1); //Normal from the camera
	
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
	float distToCamera=distance(locationWorld,camPosUniform);
	vec3 lightIncidenceVector=normalize(lightPosUniform-locationWorld);
	float distLightWorld=distance(lightPosUniform,locationWorld);
	distLightWorld+=distToCamera;
	
	float cosLightNormal=max(0,dot(lightIncidenceVector,normalCam));
	colorsete=cosLightNormal*colorsete*attenuation / (distLightWorld*distLightWorld);

	if(directLight==1) accumRadiance(screenPos,distLightWorld,colorsete);
	
	//INDIRECT LIGHT
	vec3 indirectLight=vec3(0,0,0);
	for(float w=0.5;w<lightRes_x;w++)
	{
		for(float h=0.5;h<lightRes_y;h++)
		{
			vec2 lp=vec2(w/lightRes_x,h/lightRes_y);
			vec2 pxAttenuationIndex=lp.xy;
			if(lp.x>0.5) pxAttenuationIndex.x=1-pxAttenuationIndex.x;
			if(lp.y>0.5) pxAttenuationIndex.y=1-pxAttenuationIndex.y;
			float pxAttenuation=texture(pxAngles,pxAttenuationIndex.xy).x;
	
			vec3 colorseteLight=texture(lightColor,lp).xyz;
			vec3 normalLight=(texture(camNormal,lp).xyz*2) - vec3(1,1,1);
			
			vec3 posLight=texture(lightPosition,lp).xyz;
			vec3 vecLightWorld=normalize(posLight-locationWorld);
			
			float dist=distance(locationWorld,posLight);
			float distAttenuation=dist*dist;
			dist+=distToCamera;
			dist+=distance(posLight,lightPosUniform);
			if(dist<minDistThreshold)dist=minDistThreshold;
			
			accumRadiance(screenPos,dist,(colorseteLight*max(0,dot(-vecLightWorld,normalLight))*max(0,dot(vecLightWorld,normalCam))/(distAttenuation))*colorseteBase*pxAttenuation);		// if no pxAttenuation, attenuate using /(lightRes_x*lightRes_y));
		}
	}
}