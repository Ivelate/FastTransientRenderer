package shaders;

import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import org.lwjgl.opengl.GL11;

/**
 * Shader which dumps the solid angle of each pixel of screen on a texture
 */
public class SolidAnglePrecomputationShader extends SimpleShaderProgram{

	public SolidAnglePrecomputationShader()
	{
		super("/shadersCode/fullscreenShader.vsh","/shadersCode/solidAnglePrecompShader.fsh",true);
	}
	
	/**
	 * 0,1 - location ;
	 */
	@Override
	public void setupAttributes() {
		int locAttrib=glGetAttribLocation(this.getID(),"location");
		glVertexAttribPointer(locAttrib,2,GL11.GL_FLOAT,false,4*this.getSize(),0);
		glEnableVertexAttribArray(locAttrib);
	}

	@Override
	public int getSize() {
		return 2; 
	}

	@Override
	protected void dispose() {
		// TODO Auto-generated method stub
		
	}

}
