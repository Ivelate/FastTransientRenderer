package shaders;

import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import org.lwjgl.opengl.GL11;

/**
 * Shader to render diffuse materials
 */
public class DiffuseShader extends SimpleShaderProgram{

	public DiffuseShader()
	{
		super("/shadersCode/diffuseShader.vsh","/shadersCode/diffuseShader.fsh",true);
	}
	
	/**
	 * 0,1,2 - location ; 3,4,5 - normal ;
	 */
	@Override
	public void setupAttributes() {
		int locAttrib=glGetAttribLocation(this.getID(),"location");
		glVertexAttribPointer(locAttrib,3,GL11.GL_FLOAT,false,4*this.getSize(),0);
		glEnableVertexAttribArray(locAttrib);
		
		int normAttrib=glGetAttribLocation(this.getID(),"normal");
		glVertexAttribPointer(normAttrib,3,GL11.GL_FLOAT,false,4*this.getSize(),3*4);
		glEnableVertexAttribArray(normAttrib);
	}

	@Override
	public int getSize() {
		return 6; 
	}

	@Override
	protected void dispose() {
		// TODO Auto-generated method stub
		
	}

}
