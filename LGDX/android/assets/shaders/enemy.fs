#ifdef GL_ES
	precision mediump float;
#endif
					
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
					
void main()                            
{
	vec4 col = texture2D(u_texture, v_texCoords);
	float shade = 0.21*col.r + 0.72*col.g + 0.07*col.b;

	gl_FragColor = vec4(0.59*shade, 0.521*shade, 0.447*shade, col.a);
}