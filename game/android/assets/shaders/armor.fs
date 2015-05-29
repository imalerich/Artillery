#ifdef GL_ES
	precision mediump float;
#endif
					
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
					
void main()                                  
{                                            
	gl_FragColor = vec4(43f/255f, 146/255f, 160/255f,
		texture2D(u_texture, v_texCoords).a*0.5);
}
