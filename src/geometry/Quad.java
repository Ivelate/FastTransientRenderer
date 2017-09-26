package geometry;

import static org.lwjgl.opengl.GL15.glBindBuffer;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import core.ProgramParams;
import util.Camera;
import util.MatrixHelper;

/**
 * Handles the utilities to render a Quad into the GPU
 */
public class Quad 
{
	public int vbo;
	
	public Quad()
	{
		this.vbo=GL15.glGenBuffers();
		loadIntoGPU();
	}
	
	public void loadIntoGPU()
	{
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo); 
		FloatBuffer trianguloBuffer=BufferUtils.createFloatBuffer(2*6);
		float[] quadData={-1,-1,1,-1,-1,1,-1,1,1,-1,1,1};
		trianguloBuffer.put(quadData);
		trianguloBuffer.flip(); 

		GL15.glBufferData(GL15.GL_ARRAY_BUFFER,quadData.length*4,GL15.GL_STATIC_DRAW);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, trianguloBuffer); 
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	public int getVbo(){
		return this.vbo;
	}
}
