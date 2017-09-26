#version 450 core

uniform mat4 invProjMat;
uniform vec2 screenRes;

layout(location=0) out float solidAngle;
in vec2 Location;

#define M_PI 3.1415926535897932384626433832795

void main()
{
	float pxOffsetX=1.0/screenRes.x;
	float pxOffsetY=1.0/screenRes.y;
	vec2 normLoc=vec2(1,1)-Location;
	normLoc=normLoc+(1/(screenRes*100));
	//normLoc.x=0+pxOffsetX;
	
	vec4 v00_p=invProjMat*vec4(normLoc + vec2(-pxOffsetX,-pxOffsetY),1,1);
	vec4 v10_p=invProjMat*vec4(normLoc + vec2(pxOffsetX,-pxOffsetY),1,1);
	vec4 v01_p=invProjMat*vec4(normLoc + vec2(-pxOffsetX,pxOffsetY),1,1);
	vec4 v11_p=invProjMat*vec4(normLoc + vec2(pxOffsetX,pxOffsetY),1,1);
	
	vec3 v00=v00_p.xyz/v00_p.w;
	vec3 v10=v10_p.xyz/v10_p.w;
	vec3 v01=v01_p.xyz/v01_p.w;
	vec3 v11=v11_p.xyz/v11_p.w;
	
	vec3 n0=normalize(cross(v00,v10));
	vec3 n1=normalize(cross(v10,v11));
	vec3 n2=normalize(cross(v11,v01));
	vec3 n3=normalize(cross(v01,v00));
	
	float g0=acos(-dot(n0,n1));
	float g1=acos(-dot(n1,n2));
	float g2=acos(-dot(n2,n3));
	float g3=acos(-dot(n3,n0));
	
	solidAngle=(g0+g1+g2+g3 -2*M_PI) /(4*M_PI);

	//solidAngle=Location.x;
}