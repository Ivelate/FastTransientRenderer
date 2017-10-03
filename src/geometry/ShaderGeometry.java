package geometry;

import static org.lwjgl.opengl.GL15.glBindBuffer;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;

import core.ProgramParams;
import material.Material;
import shaders.SimpleShaderProgram;
import util.Camera;
import util.MatrixHelper;

/**
 * Contains a geometry which will be rendered using a provided material and has ulity methods to load it into GPU and to render it.
 */
public class ShaderGeometry 
{
	private SimpleShaderProgram shader;
	private Material material;
	private int vbo=0;
	
	private Matrix4f modelMatrix=null;
	
	private float[] geometryData;
	
	public ShaderGeometry(Material material,float[] geometryData)
	{
		this(material,geometryData,null);
	}
	public ShaderGeometry(Material material,float[] geometryData,Matrix4f modelMatrix)
	{
		this.shader=material.getAssociatedShader();
		this.vbo=GL15.glGenBuffers();
		this.geometryData=geometryData;
		this.material=material;
		this.modelMatrix=modelMatrix;
	}
	public void loadIntoGPU()
	{
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo); 
		FloatBuffer trianguloBuffer=FloatBufferPool.requestBuffer(this.geometryData.length);

		trianguloBuffer.put(this.geometryData);
		trianguloBuffer.flip(); 

		GL15.glBufferData(GL15.GL_ARRAY_BUFFER,geometryData.length*4,GL15.GL_STATIC_DRAW);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, trianguloBuffer); 
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	public void draw(Camera cam,ProgramParams params)
	{
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo); 
		this.shader.enable();
		this.shader.setupAttributes();
		this.material.setupShader(params);
		
		MatrixHelper.uploadMatrix(cam.getProjectionViewMatrix(), GL20.glGetUniformLocation(this.shader.getID(), "viewProjectionMatrix"));
		MatrixHelper.uploadMatrix(this.modelMatrix==null?MatrixHelper.identity:this.modelMatrix, GL20.glGetUniformLocation(this.shader.getID(), "modelMatrix"));

		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, this.geometryData.length/this.shader.getSize());
		this.shader.disable();
		glBindBuffer(GL15.GL_ARRAY_BUFFER,0); 
	}
}
