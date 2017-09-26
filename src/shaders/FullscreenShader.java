package shaders;

import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import org.lwjgl.opengl.GL11;

/**
 * Used to store each poly into the 3D volume considering the time travel of the light. This is the fullscreen deferred shader. 
 */
public class FullscreenShader extends SimpleShaderProgram{

	public FullscreenShader()
	{
		super("/shadersCode/fullscreenShaderAdjustable.vsh","/shadersCode/fullscreenShader.fsh",true);
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
