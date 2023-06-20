#version 430

in vec2 tc;
in vec4 shadow_coord;
in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
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
uniform mat4 shadowMVP;
layout (binding=0) uniform sampler2D samp;
layout (binding=1) uniform sampler2DShadow shadowTex;

void main(void)
{
	vec4 texColor = texture(samp, tc);

	vec3 L = normalize(varyingLightDir);
	
	vec3 N = normalize(varyingNormal);
	
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

	vec4 lightingColor = vec4((diffuse + specular), 1.0);
	vec4 ambientColor = vec4(ambient, 1.0);

	float notInShadow = textureProj(shadowTex, shadow_coord);

	fragColor = 0.5 * texColor + 0.5 * ambientColor;

	if (notInShadow == 1.0)
	{	
		
		fragColor += 0.5 * texColor + 0.5 * lightingColor;
	}

	
}
