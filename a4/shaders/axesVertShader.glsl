#version 430

uniform mat4 v_matrix;
uniform mat4 p_matrix;
out vec4 color; 

void main(void)
{
  const vec4 vertices[6] = vec4[6]
	(vec4(0.0,0.0,0.0, 1.0),
	vec4( 15.0,0.0,0.0, 1.0),
	vec4( 0.0,0.0,0.0, 1.0),
	vec4( 0.0,15.0,0.0, 1.0),
	vec4( 0.0,0.0,0.0, 1.0),
	vec4( 0.0,0.0,15.0, 1.0));

	gl_Position = p_matrix * v_matrix * vertices[gl_VertexID];
	
	if (gl_VertexID == 0 || gl_VertexID == 1)
      color = vec4(1, 0, 0, 1.0);
    else if (gl_VertexID == 2 || gl_VertexID == 3)
      color = vec4(0, 1, 0.0, 1.0);
    else if (gl_VertexID == 4 || gl_VertexID == 5)
      color = vec4(0, 0, 1, 1.0);
  
}
