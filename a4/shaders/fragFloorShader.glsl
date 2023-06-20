#version 430

in vec2 tc;
in vec4 shadow_coord;
in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingTangent;
out vec4 fragColor;


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

vec3 calcNewNormal()
{
	vec3 normal = normalize(varyingNormal);
	vec3 tangent = normalize(varyingTangent);
	tangent = normalize(tangent - dot(tangent, normal) * normal);
	vec3 bitangent = cross(tangent, normal);
	mat3 tbn = mat3(tangent, bitangent, normal);
	vec3 retrievedNormal = texture(normMap,tc).xyz;
	retrievedNormal = retrievedNormal * 2.0 - 1.0;
	vec3 newNormal = tbn * retrievedNormal;
	newNormal = normalize(newNormal);
	return newNormal;
}

void main(void)
{
	vec4 texColor = texture(samp, tc);

	vec3 L = normalize(varyingLightDir);
	
	vec3 N = calcNewNormal();
	
	vec3 V = normalize(-v_matrix[3].xyz - varyingVertPos);

	// compute light reflection vector, with respect N:
	vec3 R = normalize(reflect(-L, N));
	
	// get the angle between the light and surface normal:
	float cosTheta = dot(L,N);
	
	// angle between the view vector and reflected light:
	float cosPhi = dot(V,R);

	// compute ADS contributions (per pixel):
	vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
	vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
	vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess); 

	vec4 lightingColor = vec4((ambient + diffuse + specular), 1.0);
	vec4 ambientColor = vec4(ambient, 1.0);

	fragColor = 0.5 * texColor + 0.5 * lightingColor;

	
}
