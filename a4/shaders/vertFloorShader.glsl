#version 430

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertNormal;
layout (location=3) in vec3 vertTangent;

out vec4 shadow_coord;
out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingTangent;
out vec3 varyingVertPos;
out vec3 originalVertex;
out vec2 tc;

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
layout (binding=0) uniform sampler2D samp;
layout (binding=2) uniform sampler2D normMap;
layout (binding=3) uniform sampler2D h; 

void main(void)
{
  // height-mapped vertex
  vec4 p = vec4(position, 1.0)  + vec4((vertNormal*((texture2D(h,texCoord).r)/5.0f)),1.0f);
  
  //output the vertex position to the rasterizer for interpolation
	varyingVertPos = (m_matrix * vec4(position,1.0)).xyz;
        
	//get a vector from the vertex to the light and output it to the rasterizer for interpolation
	varyingLightDir = light.position - varyingVertPos;

	originalVertex = position;

	//get a vertex normal vector in eye space and output it to the rasterizer for interpolation
	varyingNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;
	varyingTangent = (norm_matrix * vec4(vertTangent, 1.0)).xyz;

	gl_Position = p_matrix * v_matrix * m_matrix * p;
  	tc = texCoord;
  
}
