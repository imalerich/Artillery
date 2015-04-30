#ifdef GL_ES
	precision mediump float;
#endif
					
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
					
void main()                            
{
	gl_FragColor = vec4(0.06f, 0.06f, 0.06f,
		texture2D(u_texture, v_texCoords).a);
}