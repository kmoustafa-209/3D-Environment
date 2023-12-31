#version 430

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
out vec3 varyingNormal;
out vec3 originalPosition;
out vec3 varyingLightDir;
out vec3 varyingVertPos;

struct PositionalLight
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec3 position;
};
struct Material
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 m_matrix;
uniform mat4 v_matrix;
uniform mat4 p_matrix;
uniform mat4 norm_matrix;

uniform float alpha;
uniform float flipNormal;

layout (binding=0) uniform sampler3D s;

void main(void)
{	varyingNormal = (norm_matrix * vec4(normal,1.0)).xyz;
	originalPosition = position;
	
	//if rendering a back-face, flip the normal
	if (flipNormal < 0) varyingNormal = -varyingNormal;

	varyingVertPos = (m_matrix * vec4(position,1.0)).xyz;
	varyingLightDir = light.position - varyingVertPos;
	
	gl_Position = p_matrix * v_matrix * m_matrix * vec4(position,1.0);
}